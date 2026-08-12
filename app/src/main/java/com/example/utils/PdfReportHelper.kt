package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color as PtColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.DayReport
import com.example.data.MarketEntry
import com.example.data.PredictionResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportHelper {

    fun generateAndOpenMarketPdf(
        context: Context,
        marketName: String,
        prediction: PredictionResult?,
        sevenDayReports: List<DayReport>,
        marketEntries: List<MarketEntry>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = PtColor.parseColor("#E6B800") // Gold
                textSize = 22f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = PtColor.DKGRAY
                textSize = 12f
            }

            val headerPaint = Paint().apply {
                color = PtColor.BLACK
                textSize = 14f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = PtColor.BLACK
                textSize = 11f
            }

            val linePaint = Paint().apply {
                color = PtColor.LTGRAY
                strokeWidth = 1f
            }

            var yPos = 40f

            // App Header
            canvas.drawText("A23 PRO - AUTOMATED MARKET REPORT", 40f, yPos, titlePaint)
            yPos += 20f
            val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            canvas.drawText("Generated on: ${dateFormat.format(Date())} • Market: $marketName", 40f, yPos, subtitlePaint)
            yPos += 15f

            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 25f

            // Prediction Section
            canvas.drawText("🎯 AI PREDICTION SUMMARY ($marketName)", 40f, yPos, headerPaint)
            yPos += 20f

            if (prediction != null) {
                canvas.drawText("Main OTC Digits: ${prediction.mainOtc.joinToString(" - ")}", 50f, yPos, bodyPaint)
                yPos += 16f
                canvas.drawText("Super Jodi Combinations: ${prediction.superJodi.joinToString(", ")}", 50f, yPos, bodyPaint)
                yPos += 16f
                canvas.drawText("Recommended Safe Day: ${prediction.safeDay}", 50f, yPos, bodyPaint)
                yPos += 16f
                canvas.drawText("Confidence Accuracy Rating: ${prediction.confidence}", 50f, yPos, bodyPaint)
                yPos += 20f
            }

            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 25f

            // 7-Day Chart Analysis
            canvas.drawText("📊 7-DAY RECENT CHART HISTORY", 40f, yPos, headerPaint)
            yPos += 20f

            // Table Header
            val headerBgPaint = Paint().apply {
                color = PtColor.parseColor("#F5F5F5")
            }
            canvas.drawRect(40f, yPos - 12f, 555f, yPos + 6f, headerBgPaint)
            canvas.drawText("Date", 50f, yPos, headerPaint)
            canvas.drawText("OTC", 170f, yPos, headerPaint)
            canvas.drawText("Result Number", 300f, yPos, headerPaint)
            canvas.drawText("Status", 450f, yPos, headerPaint)
            yPos += 22f

            sevenDayReports.take(7).forEach { item ->
                canvas.drawText(item.date, 50f, yPos, bodyPaint)
                canvas.drawText(item.otc.joinToString(" "), 170f, yPos, bodyPaint)
                canvas.drawText(item.result, 300f, yPos, bodyPaint)
                canvas.drawText(if (item.isPass) "PASS" else "FAIL", 450f, yPos, bodyPaint)
                yPos += 18f
            }

            yPos += 15f
            canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
            yPos += 25f

            // Historical Raw Database Sample
            canvas.drawText("🗄️ RECORDED ENTRIES (LAST 10 RECENT)", 40f, yPos, headerPaint)
            yPos += 20f

            marketEntries.take(10).forEach { entry ->
                val displayStr = "• ${entry.date} | Result: ${entry.result} ${if (entry.isHoliday) "(Holiday)" else ""}"
                canvas.drawText(displayStr, 50f, yPos, bodyPaint)
                yPos += 16f
            }

            yPos = 810f
            canvas.drawText("A23 PRO Neural Intelligence • Confidential System Document", 150f, yPos, subtitlePaint)

            pdfDocument.finishPage(page)

            // Save PDF File locally in cache for app viewer
            val pdfDir = File(context.cacheDir, "pdf_reports")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val fileName = "A23_PRO_${marketName}_Report.pdf"
            val file = File(pdfDir, fileName)

            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Save copy to Phone Public Downloads Storage (MediaStore / External Storage)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { out ->
                            file.inputStream().use { input -> input.copyTo(out) }
                        }
                        Toast.makeText(context, "✅ PDF phone storage (Downloads/$fileName) me save ho gaya!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                        val publicFile = File(downloadsDir, fileName)
                        file.copyTo(publicFile, overwrite = true)
                        Toast.makeText(context, "✅ PDF Downloads folder me save ho gaya!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "PDF Cache created. Public save note: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Generation Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun openPdfWithExternalApp(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found on device: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
