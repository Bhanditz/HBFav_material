package me.rei_m.hbfavkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import me.rei_m.hbfavkotlin.R
import me.rei_m.hbfavkotlin.entities.BookmarkEntity
import me.rei_m.hbfavkotlin.views.widgets.bookmark.BookmarkContentsLayout
import me.rei_m.hbfavkotlin.views.widgets.bookmark.BookmarkCountTextView
import me.rei_m.hbfavkotlin.views.widgets.bookmark.BookmarkHeaderLayout

public class BookmarkFragment : Fragment(), FragmentAnimationI {

    private var mBookmarkEntity: BookmarkEntity? = null

    override var mContainerWidth: Float = 0.0f

    companion object {

        private val ARG_BOOKMARK = "ARG_BOOKMARK"

        public fun newInstance(bookmarkEntity: BookmarkEntity): BookmarkFragment {
            val fragment = BookmarkFragment()
            val args = Bundle()
            args.putSerializable(ARG_BOOKMARK, bookmarkEntity)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBookmarkEntity = arguments.getSerializable(ARG_BOOKMARK) as BookmarkEntity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)

        val bookmarkHeaderLayout = view.findViewById(R.id.layout_bookmark_header) as BookmarkHeaderLayout
        bookmarkHeaderLayout.bindView(mBookmarkEntity!!)

        val bookmarkContents = view.findViewById(R.id.layout_bookmark_contents) as BookmarkContentsLayout
        bookmarkContents.bindView(mBookmarkEntity!!)

        val bookmarkCountTextView = view.findViewById(R.id.text_bookmark_count) as BookmarkCountTextView
        bookmarkCountTextView.bindView(mBookmarkEntity!!)

        setContainer(container!!)

        return view
    }

    override fun onDetach() {
        super.onDetach()
        mBookmarkEntity = null
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val animator = createAnimatorMoveSlide(transit, enter, nextAnim, activity)
        return animator ?: super.onCreateAnimation(transit, enter, nextAnim)
    }
}
