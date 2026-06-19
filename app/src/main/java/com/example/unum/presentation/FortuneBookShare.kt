package com.example.unum.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.unum.data.export.FortuneBookPdfExporter
import com.example.unum.data.model.FortuneBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberFortuneBookShareHandler(): (FortuneBook) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        { book ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        FortuneBookPdfExporter.saveToDownloads(context, book)
                    }
                }.onSuccess { export ->
                    context.sharePdf(export.uri, export.displayName, book.coverTitle)
                    Toast.makeText(context, "PDF가 다운로드 폴더에 저장됐어요.", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(
                        context,
                        error.message ?: "PDF 저장에 실패했어요.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

private fun Context.sharePdf(uri: android.net.Uri, displayName: String, title: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { displayName })
        putExtra(Intent.EXTRA_TITLE, displayName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(sendIntent, "운세 PDF 공유")
    try {
        startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "PDF를 공유할 앱이 없어요.", Toast.LENGTH_LONG).show()
    }
}
