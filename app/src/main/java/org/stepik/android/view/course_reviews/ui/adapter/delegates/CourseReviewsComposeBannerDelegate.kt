package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.empty_default.view.*
import kotlinx.android.synthetic.main.view_course_review_compose_banner_item.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.util.changeVisibility
import org.stepik.android.domain.course_reviews.model.CourseReviewItem

class CourseReviewsComposeBannerDelegate(
    private val onCreateReviewClicked: () -> Unit
) : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_review_compose_banner_item))

    override fun isForViewType(position: Int, data: CourseReviewItem): Boolean =
        data is CourseReviewItem.ComposeBanner

    inner class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root) {
        private val bannerText = root.bannerText
        private val bannerButton = root.bannerButton
        private val bannerEmpty = root.bannerEmpty

        init {
            bannerButton.setOnClickListener { onCreateReviewClicked() }
            bannerEmpty.placeholderMessage.setText(R.string.course_reviews_empty)
        }

        override fun onBind(data: CourseReviewItem) {
            data as CourseReviewItem.ComposeBanner

            bannerButton.changeVisibility(data.canWriteReview)
            bannerText.changeVisibility(!data.canWriteReview)
            bannerEmpty.changeVisibility(data.isReviewsEmpty)
        }
    }
}