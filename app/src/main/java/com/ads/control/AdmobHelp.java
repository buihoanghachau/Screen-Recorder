//package com.ads.control;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.DisplayMetrics;
//import android.view.Display;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RatingBar;
//import android.widget.TextView;
//import com.facebook.shimmer.ShimmerFrameLayout;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdLoader;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.VideoOptions;
//import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
//import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
//import com.google.android.gms.ads.formats.MediaView;
//import com.google.android.gms.ads.formats.NativeAdOptions;
//import com.google.android.gms.ads.formats.UnifiedNativeAd;
//import com.google.android.gms.ads.formats.UnifiedNativeAdView;
//
//public class AdmobHelp {
//    public static long TimeReload = 60000;
//    private static AdmobHelp instance;
//    public static long timeLoad;
//    private AdCloseListener adCloseListener;
//    private boolean isReloaded = false;
//    PublisherInterstitialAd mPublisherInterstitialAd;
//    private UnifiedNativeAd nativeAd;
//
//    public interface AdCloseListener {
//        void onAdClosed();
//    }
//
//    public static AdmobHelp getInstance() {
//        if (instance == null) {
//            instance = new AdmobHelp();
//        }
//        return instance;
//    }
//
//    private AdmobHelp() {
//    }
//
//    public void init(Context context) {
//        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
//        this.mPublisherInterstitialAd = new PublisherInterstitialAd(context);
//        this.mPublisherInterstitialAd.setAdUnitId(context.getString(R.string.admob_full));
//        this.mPublisherInterstitialAd.setAdListener(new AdListener() {
//            /* class com.ads.control.AdmobHelp.AnonymousClass1 */
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdClicked() {
//            }
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdLeftApplication() {
//            }
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdLoaded() {
//            }
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdOpened() {
//            }
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdFailedToLoad(int i) {
//                if (!AdmobHelp.this.isReloaded) {
//                    AdmobHelp.this.isReloaded = true;
//                    AdmobHelp.this.loadInterstitialAd();
//                }
//            }
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdClosed() {
//                if (AdmobHelp.this.adCloseListener != null) {
//                    AdmobHelp.this.adCloseListener.onAdClosed();
//                    AdmobHelp.this.loadInterstitialAd();
//                }
//            }
//        });
//        loadInterstitialAd();
//    }
//
//    /* access modifiers changed from: private */
//    /* access modifiers changed from: public */
//    private void loadInterstitialAd() {
//        PublisherInterstitialAd publisherInterstitialAd = this.mPublisherInterstitialAd;
//        if (publisherInterstitialAd != null && !publisherInterstitialAd.isLoading() && !this.mPublisherInterstitialAd.isLoaded()) {
//            this.mPublisherInterstitialAd.loadAd(new PublisherAdRequest.Builder().build());
//        }
//    }
//
//    public void showInterstitialAd(AdCloseListener adCloseListener2) {
//        if (timeLoad + TimeReload >= System.currentTimeMillis()) {
//            adCloseListener2.onAdClosed();
//        } else if (canShowInterstitialAd()) {
//            this.adCloseListener = adCloseListener2;
//            this.mPublisherInterstitialAd.show();
//            timeLoad = System.currentTimeMillis();
//        } else {
//            adCloseListener2.onAdClosed();
//        }
//    }
//
//    private boolean canShowInterstitialAd() {
//        PublisherInterstitialAd publisherInterstitialAd = this.mPublisherInterstitialAd;
//        return publisherInterstitialAd != null && publisherInterstitialAd.isLoaded();
//    }
//
//    public void loadBanner(Activity activity) {
//        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) activity.findViewById(R.id.shimmer_container);
//        shimmerFrameLayout.setVisibility(0);
//        shimmerFrameLayout.startShimmer();
//        final LinearLayout linearLayout = (LinearLayout) activity.findViewById(R.id.banner_container);
//        try {
//            AdView adView = new AdView(activity);
//            adView.setAdUnitId(activity.getString(R.string.admob_banner));
//            linearLayout.addView(adView);
//            adView.setAdSize(getAdSize(activity));
//            adView.loadAd(new AdRequest.Builder().build());
//            adView.setAdListener(new AdListener() {
//                /* class com.ads.control.AdmobHelp.AnonymousClass2 */
//
//                @Override // com.google.android.gms.ads.AdListener
//                public void onAdFailedToLoad(int i) {
//                    super.onAdFailedToLoad(i);
//                    linearLayout.setVisibility(8);
//                    shimmerFrameLayout.stopShimmer();
//                    shimmerFrameLayout.setVisibility(8);
//                }
//
//                @Override // com.google.android.gms.ads.AdListener
//                public void onAdLoaded() {
//                    shimmerFrameLayout.stopShimmer();
//                    shimmerFrameLayout.setVisibility(8);
//                    linearLayout.setVisibility(0);
//                }
//            });
//        } catch (Exception unused) {
//        }
//    }
//
//    private AdSize getAdSize(Activity activity) {
//        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        defaultDisplay.getMetrics(displayMetrics);
//        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, (int) (((float) displayMetrics.widthPixels) / displayMetrics.density));
//    }
//
//    public void loadBannerFragment(Activity activity, View view) {
//        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_container);
//        shimmerFrameLayout.setVisibility(0);
//        shimmerFrameLayout.startShimmer();
//        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.banner_container);
//        try {
//            AdView adView = new AdView(activity);
//            adView.setAdUnitId(activity.getString(R.string.admob_banner));
//            linearLayout.addView(adView);
//            adView.setAdSize(getAdSize(activity));
//            adView.loadAd(new AdRequest.Builder().build());
//            adView.setAdListener(new AdListener() {
//                /* class com.ads.control.AdmobHelp.AnonymousClass3 */
//
//                @Override // com.google.android.gms.ads.AdListener
//                public void onAdFailedToLoad(int i) {
//                    super.onAdFailedToLoad(i);
//                    linearLayout.setVisibility(8);
//                    shimmerFrameLayout.stopShimmer();
//                    shimmerFrameLayout.setVisibility(8);
//                }
//
//                @Override // com.google.android.gms.ads.AdListener
//                public void onAdLoaded() {
//                    shimmerFrameLayout.stopShimmer();
//                    shimmerFrameLayout.setVisibility(8);
//                    linearLayout.setVisibility(0);
//                }
//            });
//        } catch (Exception unused) {
//        }
//    }
//
//    public void loadNative(final Activity activity) {
//        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) activity.findViewById(R.id.shimmer_container);
//        shimmerFrameLayout.setVisibility(0);
//        shimmerFrameLayout.startShimmer();
//        new AdLoader.Builder(activity, activity.getString(R.string.admob_native)).forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
//            /* class com.ads.control.AdmobHelp.AnonymousClass5 */
//
//            @Override // com.google.android.gms.ads.formats.UnifiedNativeAd.OnUnifiedNativeAdLoadedListener
//            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                if (AdmobHelp.this.nativeAd != null) {
//                    AdmobHelp.this.nativeAd.destroy();
//                }
//                shimmerFrameLayout.stopShimmer();
//                shimmerFrameLayout.setVisibility(8);
//                AdmobHelp.this.nativeAd = unifiedNativeAd;
//                FrameLayout frameLayout = (FrameLayout) activity.findViewById(R.id.fl_adplaceholder);
//                if (frameLayout != null) {
//                    frameLayout.setVisibility(0);
//                    UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) activity.getLayoutInflater().inflate(R.layout.native_admob_ad, (ViewGroup) null);
//                    AdmobHelp.this.populateUnifiedNativeAdView(unifiedNativeAd, unifiedNativeAdView);
//                    frameLayout.removeAllViews();
//                    frameLayout.addView(unifiedNativeAdView);
//                }
//            }
//        }).withAdListener(new AdListener() {
//            /* class com.ads.control.AdmobHelp.AnonymousClass4 */
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdFailedToLoad(int i) {
//                shimmerFrameLayout.stopShimmer();
//                shimmerFrameLayout.setVisibility(8);
//            }
//        }).withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(false).build()).build()).build().loadAd(new PublisherAdRequest.Builder().build());
//    }
//
//    public void loadNativeFragment(final Activity activity, final View view) {
//        final ShimmerFrameLayout shimmerFrameLayout = (ShimmerFrameLayout) view.findViewById(R.id.shimmer_container);
//        shimmerFrameLayout.setVisibility(0);
//        shimmerFrameLayout.startShimmer();
//        new AdLoader.Builder(activity, activity.getString(R.string.admob_native)).forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
//            /* class com.ads.control.AdmobHelp.AnonymousClass7 */
//
//            @Override // com.google.android.gms.ads.formats.UnifiedNativeAd.OnUnifiedNativeAdLoadedListener
//            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                if (AdmobHelp.this.nativeAd != null) {
//                    AdmobHelp.this.nativeAd.destroy();
//                }
//                shimmerFrameLayout.stopShimmer();
//                shimmerFrameLayout.setVisibility(8);
//                AdmobHelp.this.nativeAd = unifiedNativeAd;
//                FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.fl_adplaceholder);
//                if (frameLayout != null) {
//                    frameLayout.setVisibility(0);
//                    UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) activity.getLayoutInflater().inflate(R.layout.native_admob_ad, (ViewGroup) null);
//                    AdmobHelp.this.populateUnifiedNativeAdView(unifiedNativeAd, unifiedNativeAdView);
//                    frameLayout.removeAllViews();
//                    frameLayout.addView(unifiedNativeAdView);
//                }
//            }
//        }).withAdListener(new AdListener() {
//            /* class com.ads.control.AdmobHelp.AnonymousClass6 */
//
//            @Override // com.google.android.gms.ads.AdListener
//            public void onAdFailedToLoad(int i) {
//                shimmerFrameLayout.stopShimmer();
//                shimmerFrameLayout.setVisibility(8);
//            }
//        }).withNativeAdOptions(new NativeAdOptions.Builder().setVideoOptions(new VideoOptions.Builder().setStartMuted(false).build()).build()).build().loadAd(new PublisherAdRequest.Builder().build());
//    }
//
//    /* access modifiers changed from: private */
//    /* access modifiers changed from: public */
//    private void populateUnifiedNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView unifiedNativeAdView) {
//        unifiedNativeAdView.setMediaView((MediaView) unifiedNativeAdView.findViewById(R.id.ad_media));
//        unifiedNativeAdView.setHeadlineView(unifiedNativeAdView.findViewById(R.id.ad_headline));
//        unifiedNativeAdView.setBodyView(unifiedNativeAdView.findViewById(R.id.ad_body));
//        unifiedNativeAdView.setCallToActionView(unifiedNativeAdView.findViewById(R.id.ad_call_to_action));
//        unifiedNativeAdView.setIconView(unifiedNativeAdView.findViewById(R.id.ad_app_icon));
//        unifiedNativeAdView.setPriceView(unifiedNativeAdView.findViewById(R.id.ad_price));
//        unifiedNativeAdView.setStarRatingView(unifiedNativeAdView.findViewById(R.id.ad_stars));
//        unifiedNativeAdView.setStoreView(unifiedNativeAdView.findViewById(R.id.ad_store));
//        unifiedNativeAdView.setAdvertiserView(unifiedNativeAdView.findViewById(R.id.ad_advertiser));
//        ((TextView) unifiedNativeAdView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
//        if (unifiedNativeAd.getBody() == null) {
//            unifiedNativeAdView.getBodyView().setVisibility(4);
//        } else {
//            unifiedNativeAdView.getBodyView().setVisibility(0);
//            ((TextView) unifiedNativeAdView.getBodyView()).setText(unifiedNativeAd.getBody());
//        }
//        if (unifiedNativeAd.getCallToAction() == null) {
//            unifiedNativeAdView.getCallToActionView().setVisibility(4);
//        } else {
//            unifiedNativeAdView.getCallToActionView().setVisibility(0);
//            ((TextView) unifiedNativeAdView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
//        }
//        if (unifiedNativeAd.getIcon() == null) {
//            unifiedNativeAdView.getIconView().setVisibility(8);
//        } else {
//            ((ImageView) unifiedNativeAdView.getIconView()).setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
//            unifiedNativeAdView.getIconView().setVisibility(0);
//        }
//        if (unifiedNativeAd.getPrice() == null) {
//            unifiedNativeAdView.getPriceView().setVisibility(4);
//        } else {
//            unifiedNativeAdView.getPriceView().setVisibility(0);
//            ((TextView) unifiedNativeAdView.getPriceView()).setText(unifiedNativeAd.getPrice());
//        }
//        if (unifiedNativeAd.getStore() == null) {
//            unifiedNativeAdView.getStoreView().setVisibility(4);
//        } else {
//            unifiedNativeAdView.getStoreView().setVisibility(0);
//            ((TextView) unifiedNativeAdView.getStoreView()).setText(unifiedNativeAd.getStore());
//        }
//        if (unifiedNativeAd.getStarRating() == null) {
//            unifiedNativeAdView.getStarRatingView().setVisibility(4);
//        } else {
//            ((RatingBar) unifiedNativeAdView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
//            unifiedNativeAdView.getStarRatingView().setVisibility(0);
//        }
//        if (unifiedNativeAd.getAdvertiser() == null) {
//            unifiedNativeAdView.getAdvertiserView().setVisibility(4);
//        } else {
//            ((TextView) unifiedNativeAdView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
//            unifiedNativeAdView.getAdvertiserView().setVisibility(0);
//        }
//        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
//    }
//
//    public void destroyNative() {
//        UnifiedNativeAd unifiedNativeAd = this.nativeAd;
//        if (unifiedNativeAd != null) {
//            unifiedNativeAd.destroy();
//        }
//    }
//}
