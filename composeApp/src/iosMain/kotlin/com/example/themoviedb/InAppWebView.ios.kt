package com.example.themoviedb

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun InAppWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration(),
            ).apply {
                loadRequestForUrl(url)
            }
        },
        update = { webView ->
            val current = webView.URL?.absoluteString
            if (current != url) {
                webView.loadRequestForUrl(url)
            }
        },
    )
}

private fun WKWebView.loadRequestForUrl(url: String) {
    val nsUrl = NSURL(string = url) ?: return
    loadRequest(NSURLRequest.requestWithURL(nsUrl))
}
