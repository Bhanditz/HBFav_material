package me.rei_m.hbfavmaterial.domain.service.impl

import android.content.SharedPreferences
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import me.rei_m.hbfavmaterial.domain.entity.BookmarkEditEntity
import me.rei_m.hbfavmaterial.domain.entity.OAuthTokenEntity
import me.rei_m.hbfavmaterial.domain.service.HatenaService
import me.rei_m.hbfavmaterial.extension.subscribeAsync
import me.rei_m.hbfavmaterial.infra.network.HatenaOAuthApiService
import me.rei_m.hbfavmaterial.infra.network.HatenaOAuthManager
import me.rei_m.hbfavmaterial.infra.network.RetrofitManager
import retrofit2.HttpException
import java.net.HttpURLConnection

class HatenaServiceImpl(private val preferences: SharedPreferences,
                        private val hatenaOAuthManager: HatenaOAuthManager) : HatenaService {

    companion object {

        private const val KEY_PREF_OAUTH = "KEY_PREF_OAUTH"

        private const val MAX_TAGS_COUNT = 10
    }

    private val completeFetchRequestTokenEventSubject = PublishSubject.create<String>()

    override val completeFetchRequestTokenEvent: Observable<String> = completeFetchRequestTokenEventSubject

    private val completeRegisterAccessTokenEventSubject = PublishSubject.create<Unit>()

    override val completeRegisterAccessTokenEvent: Observable<Unit> = completeRegisterAccessTokenEventSubject

    private val completeDeleteAccessTokenEventSubject = PublishSubject.create<Unit>()

    override val completeDeleteAccessTokenEvent: Observable<Unit> = completeDeleteAccessTokenEventSubject

    private val confirmAuthorisedEventSubject = PublishSubject.create<Boolean>()

    override val confirmAuthorisedEvent: Observable<Boolean> = confirmAuthorisedEventSubject

    private val completeFindBookmarkByUrlEventSubject = PublishSubject.create<BookmarkEditEntity>()

    override val completeFindBookmarkByUrlEvent: Observable<BookmarkEditEntity> = completeFindBookmarkByUrlEventSubject

    private val completeRegisterBookmarkEventSubject = PublishSubject.create<Unit>()

    override val completeRegisterBookmarkEvent: Observable<Unit> = completeRegisterBookmarkEventSubject

    private val completeDeleteBookmarkEventSubject = PublishSubject.create<Unit>()

    override val completeDeleteBookmarkEvent: Observable<Unit> = completeDeleteBookmarkEventSubject

    private val failAuthorizeHatenaEventSubject = PublishSubject.create<Unit>()

    override val failAuthorizeHatenaEvent: Observable<Unit> = failAuthorizeHatenaEventSubject

    private val errorSubject = PublishSubject.create<Unit>()

    override val error: Observable<Unit> = errorSubject

    override fun fetchRequestToken() {
        Single.create<String> {
            it.onSuccess(hatenaOAuthManager.retrieveRequestToken())
        }.subscribeAsync({
            if (it != null) {
                completeFetchRequestTokenEventSubject.onNext(it)
            } else {
                failAuthorizeHatenaEventSubject.onNext(Unit)
            }
        }, {
            errorSubject.onNext(Unit)
        })
    }

    override fun registerAccessToken(requestToken: String) {
        Single.create<Boolean> {
            if (hatenaOAuthManager.retrieveAccessToken(requestToken)) {
                val token = OAuthTokenEntity(hatenaOAuthManager.consumer.token, hatenaOAuthManager.consumer.tokenSecret)
                preferences.edit()
                        .putString(KEY_PREF_OAUTH, Gson().toJson(token))
                        .apply()
                it.onSuccess(true)
            } else {
                it.onSuccess(false)
            }
        }.subscribeAsync({
            if (it) {
                completeRegisterAccessTokenEventSubject.onNext(Unit)
            } else {
                failAuthorizeHatenaEventSubject.onNext(Unit)
            }
        }, {
            errorSubject.onNext(Unit)
        })
    }

    override fun deleteAccessToken() {
        preferences.edit().remove(KEY_PREF_OAUTH).apply()
        completeDeleteAccessTokenEventSubject.onNext(Unit)
    }

    override fun confirmAuthorised() {
        val token = getTokenFromPreferences()
        confirmAuthorisedEventSubject.onNext(token.isAuthorised)
    }

    override fun findBookmarkByUrl(urlString: String) {

        val oAuthToken = getTokenFromPreferences()

        if (!oAuthToken.isAuthorised) {
            failAuthorizeHatenaEventSubject.onNext(Unit)
            return
        }

        hatenaOAuthManager.consumer.setTokenWithSecret(oAuthToken.token, oAuthToken.secretToken)

        val retrofit = RetrofitManager.createOAuthRetrofit(hatenaOAuthManager.consumer)

        retrofit.create(HatenaOAuthApiService::class.java).getBookmark(urlString).map { (_, private, _, _, tags, _, comment) ->
            return@map BookmarkEditEntity(url = urlString,
                    isFirstEdit = false,
                    comment = comment,
                    isPrivate = private,
                    tags = tags)
        }.onErrorResumeNext {
            return@onErrorResumeNext if (it is HttpException) {
                when (it.code()) {
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        Single.just(BookmarkEditEntity(url = urlString, isFirstEdit = true))
                    }
                    else -> {
                        Single.error(it)
                    }
                }
            } else {
                Single.error(it)
            }
        }.subscribeAsync({
            completeFindBookmarkByUrlEventSubject.onNext(it)
        }, {
            errorSubject.onNext(Unit)
        })
    }

    override fun registerBookmark(urlString: String,
                                  comment: String,
                                  isOpen: Boolean,
                                  isReadAfter: Boolean,
                                  tags: List<String>) {

        val oAuthToken = getTokenFromPreferences()

        if (!oAuthToken.isAuthorised) {
            failAuthorizeHatenaEventSubject.onNext(Unit)
            return
        }

        val postTags = tags.toMutableList()
        if (isReadAfter) {
            if (!postTags.contains(HatenaService.TAG_READ_AFTER)) {
                postTags.add(HatenaService.TAG_READ_AFTER)
            }
        } else {
            if (postTags.contains(HatenaService.TAG_READ_AFTER)) {
                postTags.remove(HatenaService.TAG_READ_AFTER)
            }
        }

        require(postTags.size <= MAX_TAGS_COUNT) { "登録可能なタグは $MAX_TAGS_COUNT 個までです。" }

        hatenaOAuthManager.consumer.setTokenWithSecret(oAuthToken.token, oAuthToken.secretToken)

        val retrofit = RetrofitManager.createOAuthRetrofit(hatenaOAuthManager.consumer)

        val isOpenValue = if (isOpen) "0" else "1"

        retrofit.create(HatenaOAuthApiService::class.java).postBookmark(urlString, comment, isOpenValue, postTags.toTypedArray()).subscribeAsync({
            completeRegisterBookmarkEventSubject.onNext(Unit)
        }, {
            errorSubject.onNext(Unit)
        })
    }

    override fun deleteBookmark(urlString: String) {

        val oAuthToken = getTokenFromPreferences()

        if (!oAuthToken.isAuthorised) {
            failAuthorizeHatenaEventSubject.onNext(Unit)
            return
        }

        hatenaOAuthManager.consumer.setTokenWithSecret(oAuthToken.token, oAuthToken.secretToken)

        val retrofit = RetrofitManager.createOAuthRetrofit(hatenaOAuthManager.consumer)

        retrofit.create(HatenaOAuthApiService::class.java).deleteBookmark(urlString).subscribeAsync({
            completeDeleteBookmarkEventSubject.onNext(Unit)
        }, {
            if (it is HttpException) {
                // 見つからなかった場合はすでに消えているので成功したとみなす.
                if (it.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                    completeDeleteBookmarkEventSubject.onNext(Unit)
                    return@subscribeAsync
                }
            }
            errorSubject.onNext(Unit)
        })
    }

    private fun getTokenFromPreferences(): OAuthTokenEntity {
        val oauthJsonString = preferences.getString(KEY_PREF_OAUTH, null)
        return if (oauthJsonString != null) {
            Gson().fromJson(oauthJsonString, OAuthTokenEntity::class.java)
        } else {
            OAuthTokenEntity()
        }
    }
}