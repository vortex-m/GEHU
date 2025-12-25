package com.example.demolition.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebSettings

/**
 * Custom WebView for rendering LaTeX mathematical notation using MathJax.
 */
class MathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {
        // Configure WebView settings for optimal math rendering
        settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = false
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        
        // Set transparent background
        setBackgroundColor(0x00000000)
    }

    /**
     * Render LaTeX content with MathJax
     */
    fun setLatex(latexText: String) {
        val html = generateMathJaxHtml(latexText)
        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }

    private fun generateMathJaxHtml(text: String): String {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        body {
            font-family: sans-serif;
            font-size: 16px;
            color: #000000;
            padding: 0;
            margin: 0;
            background: transparent;
            line-height: 1.5;
        }
        /* Ensure math displays inline with text */
        .MathJax {
            display: inline !important;
        }
    </style>
    <script type="text/javascript" async
        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
    </script>
    <script type="text/x-mathjax-config">
        MathJax.Hub.Config({
            tex2jax: {
                inlineMath: [['$','$'], ['\\(','\\)']],
                displayMath: [['$$','$$'], ['\\[','\\]']],
                processEscapes: true
            },
            "HTML-CSS": { 
                scale: 100,
                linebreaks: { automatic: true }
            },
            CommonHTML: { 
                scale: 100,
                linebreaks: { automatic: true }
            },
            SVG: {
                scale: 100,
                linebreaks: { automatic: true }
            }
        });
    </script>
</head>
<body>
${text.replace("\n", "<br>")}
</body>
</html>
        """.trimIndent()
    }
}
