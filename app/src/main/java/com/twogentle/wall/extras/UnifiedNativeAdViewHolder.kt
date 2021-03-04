package com.twogentle.wall.extras

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.twogentle.wall.R

class UnifiedNativeAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var adView :UnifiedNativeAdView = view.findViewById(R.id.ad_view)

    init {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    }

    fun getAdView(): UnifiedNativeAdView {
        return adView
    }

}