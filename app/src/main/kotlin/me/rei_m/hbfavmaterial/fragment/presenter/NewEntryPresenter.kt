package me.rei_m.hbfavmaterial.fragment.presenter

import android.support.v4.app.Fragment
import me.rei_m.hbfavmaterial.entity.EntryEntity
import me.rei_m.hbfavmaterial.enum.EntryTypeFilter
import me.rei_m.hbfavmaterial.fragment.BaseFragment
import me.rei_m.hbfavmaterial.manager.ActivityNavigator
import me.rei_m.hbfavmaterial.service.EntryService
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class NewEntryPresenter(private val view: NewEntryContact.View,
                        var entryTypeFilter: EntryTypeFilter) : NewEntryContact.Actions {

    @Inject
    lateinit var navigator: ActivityNavigator

    @Inject
    lateinit var entryService: EntryService

    private var isLoading = false

    init {
        (view as BaseFragment).component.inject(this)
    }

    override fun clickEntry(entryEntity: EntryEntity) {
        val activity = (view as Fragment).activity
        navigator.navigateToBookmark(activity, entryEntity)
    }

    override fun initializeListContents(): Subscription? {

        if (isLoading) return null

        view.showProgress()

        return request()
    }

    override fun fetchListContents(): Subscription? {

        if (isLoading) return null

        return request()
    }

    override fun toggleListContents(entryTypeFilter: EntryTypeFilter): Subscription? {

        if (isLoading) return null

        if (this.entryTypeFilter == entryTypeFilter) return null

        this.entryTypeFilter = entryTypeFilter

        return request()
    }

    private fun request(): Subscription? {

        return entryService.findNewEntryByType(entryTypeFilter)
                .doOnSubscribe {
                    isLoading = true
                }
                .doOnUnsubscribe {
                    isLoading = false
                    view.hideProgress()
                }
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onFindNewEntryByTypeSuccess(it)
                }, {
                    onFindNewEntryByTypeFailure(it)
                })
    }

    private fun onFindNewEntryByTypeSuccess(entryList: List<EntryEntity>) {
        if (entryList.isEmpty()) {
            view.hideEntryList()
            view.showEmpty()
        } else {
            view.hideEmpty()
            view.showEntryList(entryList)
        }
    }

    private fun onFindNewEntryByTypeFailure(e: Throwable) {
        view.showNetworkErrorMessage()
    }
}
