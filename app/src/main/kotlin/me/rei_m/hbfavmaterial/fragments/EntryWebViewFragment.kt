package me.rei_m.hbfavmaterial.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import me.rei_m.hbfavmaterial.R

/**
 * 記事のコンテンツをWebViewに表示するFragment.
 */
class EntryWebViewFragment : Fragment() {

    private val mEntryUrl: String by lazy {
        arguments.getString(ARG_ENTRY_URL)
    }

    companion object {

        val TAG = EntryWebViewFragment::class.java.simpleName

        private val ARG_ENTRY_URL = "ARG_ENTRY_URL"

        fun newInstance(entryUrl: String): EntryWebViewFragment {
            return EntryWebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ENTRY_URL, entryUrl)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_bookmark_webview, container, false)

        with(view.findViewById(R.id.fragment_bookmark_webview_view)) {
            this as WebView
            with(settings) {
                javaScriptEnabled = true
                builtInZoomControls = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setGeolocationEnabled(true)
                setSupportMultipleWindows(true)
            }
            setWebChromeClient(WebChromeClient())
            setWebViewClient(object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            })
            loadUrl(mEntryUrl)
        }

        return view
    }

    /**
     * WebView内のコンテンツがヒストリバック可能な場合、ヒストリバックして表示する.
     *
     * @return ヒストリバックしない場合 true, した場合 false
     */
    fun backHistory(): Boolean {

        val view = view ?: return true
        
        with(view.findViewById(R.id.fragment_bookmark_webview_view)) {
            this as WebView
            if (canGoBack()) {
                goBack()
                return false
            } else {
                return true
            }
        }
    }
}