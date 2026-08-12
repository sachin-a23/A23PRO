package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.HistoryRecordItem
import com.example.ui.screens.A23LabHistoryCard
import com.example.ui.screens.DayBacktestRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    /**
     * Generates a digital, colorful PDF report for A23 Formula Lab history cards across all pages
     */
    fun exportA23HistoryToPdf(
        context: Context,
        reportTitle: String,
        marketFilter: String,
        historyCards: List<A23LabHistoryCard>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            fun drawPageHeader(canvas: Canvas, pNum: Int) {
                // Header Banner (Dark Navy with Cyber Gold Top Accent Line)
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

                paint.color = Color.parseColor("#FFD700") // Gold accent top line
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

                // Header Title
                paint.color = Color.parseColor("#FFD700")
                paint.textSize = 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("⚡ A23 DIGITAL FORMULA REPORT", 24f, 42f, paint)

                // Header Subtitle
                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Market Filter: $marketFilter  •  Generated: $currentDateStr", 24f, 65f, paint)

                paint.color = Color.parseColor("#00E5FF") // Cyan highlight
                paint.textSize = 10f
                canvas.drawText("Official A23 Settings & Formula Lab History Pass Report (Page $pNum)", 24f, 85f, paint)

                // Watermark Seal Badge
                paint.color = Color.parseColor("#1E293B")
                val sealRect = RectF(pageWidth - 140f, 20f, pageWidth - 20f, 90f)
                canvas.drawRoundRect(sealRect, 10f, 10f, paint)

                paint.color = Color.parseColor("#00E676")
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("A23 CERTIFIED", pageWidth - 130f, 48f, paint)
                paint.color = Color.WHITE
                paint.textSize = 9f
                canvas.drawText("${historyCards.size} Total Cards", pageWidth - 130f, 68f, paint)
            }

            fun drawPageFooter(canvas: Canvas, pNum: Int) {
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

                paint.color = Color.WHITE
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Digital A23 Formula Lab • All Market Pass Report • Page $pNum", 24f, pageHeight - 16f, paint)
            }

            drawPageHeader(canvas, pageNumber)

            var currentY = 130f
            val cardHeight = 110f

            if (historyCards.isEmpty()) {
                paint.color = Color.DKGRAY
                paint.textSize = 14f
                canvas.drawText("No history cards available for market: $marketFilter", 24f, currentY + 30f, paint)
            } else {
                // Process ALL cards without slicing/truncating
                historyCards.forEachIndexed { index, card ->
                    if (currentY + cardHeight > pageHeight - 50) {
                        drawPageFooter(canvas, pageNumber)
                        pdfDocument.finishPage(page)

                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        drawPageHeader(canvas, pageNumber)
                        currentY = 130f
                    }

                    // Card Background
                    paint.color = Color.parseColor("#F8FAFC")
                    val bgRect = RectF(20f, currentY, pageWidth - 20f, currentY + cardHeight)
                    canvas.drawRoundRect(bgRect, 12f, 12f, paint)

                    // Left Side Market Color Pill
                    paint.color = if (index % 2 == 0) Color.parseColor("#1E1B4B") else Color.parseColor("#064E3B")
                    val pillRect = RectF(20f, currentY, 32f, currentY + cardHeight)
                    canvas.drawRoundRect(pillRect, 6f, 6f, paint)

                    // Market Name Badge
                    paint.color = Color.parseColor("#DAA520")
                    val badgeRect = RectF(42f, currentY + 12f, 160f, currentY + 32f)
                    canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

                    paint.color = Color.BLACK
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(card.marketName.take(16), 48f, currentY + 26f, paint)

                    // Formula Name
                    paint.color = Color.parseColor("#0F172A")
                    paint.textSize = 12f
                    canvas.drawText(card.formulaName, 170f, currentY + 26f, paint)

                    // Rule Details
                    paint.color = Color.parseColor("#475569")
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Rule: ${card.ruleDetails}", 42f, currentY + 46f, paint)

                    // OTC Digits (Green Box)
                    paint.color = Color.parseColor("#DCFCE7")
                    val otcBox = RectF(42f, currentY + 56f, 260f, currentY + 98f)
                    canvas.drawRoundRect(otcBox, 8f, 8f, paint)

                    paint.color = Color.parseColor("#15803D")
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("OTC Digits:", 50f, currentY + 72f, paint)
                    paint.textSize = 14f
                    canvas.drawText(card.otcList.joinToString(" - "), 50f, currentY + 92f, paint)

                    // Super Jodi (Gold Box)
                    paint.color = Color.parseColor("#FEF3C7")
                    val jodiBox = RectF(270f, currentY + 56f, 440f, currentY + 98f)
                    canvas.drawRoundRect(jodiBox, 8f, 8f, paint)

                    paint.color = Color.parseColor("#B45309")
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("Super Jodi:", 278f, currentY + 72f, paint)
                    paint.textSize = 13f
                    canvas.drawText(card.superJodiList.joinToString(" , "), 278f, currentY + 92f, paint)

                    // Accuracy & Best Day
                    paint.color = Color.parseColor("#16A34A")
                    paint.textSize = 11f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("Pass: ${card.accuracyPercentage}", 450f, currentY + 72f, paint)

                    paint.color = Color.parseColor("#0284C7")
                    paint.textSize = 8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val bestDayClean = card.bestNeverFailDay.take(20)
                    canvas.drawText("Best: $bestDayClean", 450f, currentY + 90f, paint)

                    currentY += cardHeight + 12f
                }
            }

            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)

            val fileName = "A23_Digital_Report_${marketFilter.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val savedFile = savePdfToPublicDownloads(context, pdfDocument, fileName)
            pdfDocument.close()

            if (savedFile != null) {
                openOrSharePdfFile(context, savedFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF बनाने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates a digital, multi-page PDF report for ALL-DAYS Formula Backtest History,
     * including Date, Day, Live Result, Predicted OTC, Predicted Jodi, Predicted Panel, and PASS/FAIL Mark.
     */
    fun exportAllDaysFormulaBacktestToPdf(
        context: Context,
        marketName: String,
        formulaName: String,
        ruleDetails: String,
        totalDays: Int,
        passCount: Int,
        failCount: Int,
        passRate: String,
        bestDay: String,
        records: List<DayBacktestRecord>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            fun drawHeaderBanner(canvas: Canvas, pNum: Int): Float {
                // Header Banner
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 115f, paint)

                // Gold Top Accent Line
                paint.color = Color.parseColor("#FFD700")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

                // Title
                paint.color = Color.parseColor("#FFD700")
                paint.textSize = 18f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("⚡ A23 ALL-DAYS DIGITAL FORMULA REPORT", 18f, 32f, paint)

                // Market & Rule
                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("MARKET: $marketName  •  $formulaName", 18f, 52f, paint)

                paint.color = Color.parseColor("#94A3B8")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Rules: $ruleDetails", 18f, 68f, paint)

                // Pass Stats Box
                paint.color = Color.parseColor("#00E676")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Total Tested: $totalDays Days  |  Passed: $passCount ✓  |  Failed: $failCount ✗  |  Pass Rate: $passRate", 18f, 86f, paint)

                paint.color = Color.parseColor("#00E5FF")
                paint.textSize = 9f
                canvas.drawText("Best Day: $bestDay  •  Generated: $currentDateStr (Page $pNum)", 18f, 102f, paint)

                // Table Header Column Row
                val headerY = 125f
                paint.color = Color.parseColor("#1E293B")
                val tableHeaderRect = RectF(14f, headerY, pageWidth - 14f, headerY + 26f)
                canvas.drawRoundRect(tableHeaderRect, 5f, 5f, paint)

                paint.color = Color.parseColor("#FFD700")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Date", 20f, headerY + 17f, paint)
                canvas.drawText("Day", 85f, headerY + 17f, paint)
                canvas.drawText("Live Result", 145f, headerY + 17f, paint)
                canvas.drawText("OTC", 230f, headerY + 17f, paint)
                canvas.drawText("Pred Jodi", 295f, headerY + 17f, paint)
                canvas.drawText("Pred Panel", 370f, headerY + 17f, paint)
                canvas.drawText("Status", 495f, headerY + 17f, paint)

                return headerY + 32f
            }

            fun drawFooter(canvas: Canvas, pNum: Int) {
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, pageHeight - 35f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

                paint.color = Color.WHITE
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("A23 Digital Formula Lab • All-Days Pass/Fail Report • $marketName • Page $pNum", 18f, pageHeight - 14f, paint)
            }

            var currentY = drawHeaderBanner(canvas, pageNumber)

            records.forEach { item ->
                if (currentY > pageHeight - 55f) {
                    drawFooter(canvas, pageNumber)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = drawHeaderBanner(canvas, pageNumber)
                }

                // Row Background
                paint.color = if (item.isOverallPass) Color.parseColor("#F0FDF4") else Color.parseColor("#FEF2F2")
                val rowRect = RectF(14f, currentY, pageWidth - 14f, currentY + 25f)
                canvas.drawRoundRect(rowRect, 4f, 4f, paint)

                // Border Line
                paint.color = if (item.isOverallPass) Color.parseColor("#BBF7D0") else Color.parseColor("#FECACA")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(rowRect, 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Cell Text Content
                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item.date, 20f, currentY + 16f, paint)
                canvas.drawText(item.dayOfWeek, 85f, currentY + 16f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.parseColor("#0F172A")
                canvas.drawText(item.actualResult, 145f, currentY + 16f, paint)

                paint.color = Color.parseColor("#15803D")
                canvas.drawText(item.predictedOtc.joinToString(","), 230f, currentY + 16f, paint)

                paint.color = Color.parseColor("#B45309")
                canvas.drawText(item.predictedJodi.take(2).joinToString(","), 295f, currentY + 16f, paint)

                paint.color = Color.parseColor("#0369A1")
                canvas.drawText(item.predictedPanelFormat, 370f, currentY + 16f, paint)

                if (item.isOverallPass) {
                    paint.color = Color.parseColor("#16A34A")
                    canvas.drawText("PASS ✓", 495f, currentY + 16f, paint)
                } else {
                    paint.color = Color.parseColor("#DC2626")
                    canvas.drawText("FAIL ✗", 495f, currentY + 16f, paint)
                }

                currentY += 28f
            }

            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)

            val fileName = "A23_AllDays_${marketName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val savedFile = savePdfToPublicDownloads(context, pdfDocument, fileName)
            pdfDocument.close()

            if (savedFile != null) {
                openOrSharePdfFile(context, savedFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "All-Days PDF बनाने में त्रुटि: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates a digital, colorful PDF report for ALL general historical logs across multiple pages
     */
    fun exportGeneralHistoryToPdf(
        context: Context,
        marketName: String,
        formulaName: String,
        records: List<HistoryRecordItem>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            val passCount = records.count { it.isPass }
            val passRate = if (records.isNotEmpty()) (passCount * 100 / records.size) else 0

            fun drawTableHeaderAndBanner(canvas: Canvas, pNum: Int): Float {
                // Header Banner
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

                paint.color = Color.parseColor("#00E5FF") // Cyan top accent
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

                paint.color = Color.parseColor("#FFD700")
                paint.textSize = 20f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("📊 MARKET HISTORICAL REPORT ($marketName)", 24f, 42f, paint)

                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Formula Engine: $formulaName  •  Generated: $currentDateStr (Page $pNum)", 24f, 65f, paint)

                paint.color = Color.parseColor("#00E676")
                paint.textSize = 10f
                canvas.drawText("Total Saved Records: ${records.size}  |  Pass Count: $passCount  |  Pass Rate: $passRate%", 24f, 85f, paint)

                // Table Header
                val headerY = 130f
                paint.color = Color.parseColor("#1E293B")
                val tableHeaderRect = RectF(20f, headerY, pageWidth - 20f, headerY + 28f)
                canvas.drawRoundRect(tableHeaderRect, 6f, 6f, paint)

                paint.color = Color.WHITE
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Date", 32f, headerY + 18f, paint)
                canvas.drawText("Day", 120f, headerY + 18f, paint)
                canvas.drawText("Result", 220f, headerY + 18f, paint)
                canvas.drawText("Predicted OTC", 320f, headerY + 18f, paint)
                canvas.drawText("Status", 480f, headerY + 18f, paint)

                return headerY + 34f
            }

            fun drawFooter(canvas: Canvas, pNum: Int) {
                paint.color = Color.parseColor("#0F172A")
                canvas.drawRect(0f, pageHeight - 40f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

                paint.color = Color.WHITE
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Digital Historical Report • $marketName • Page $pNum", 24f, pageHeight - 16f, paint)
            }

            var currentY = drawTableHeaderAndBanner(canvas, pageNumber)

            // Process ALL records without limit
            records.forEach { item ->
                if (currentY > pageHeight - 65) {
                    drawFooter(canvas, pageNumber)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = drawTableHeaderAndBanner(canvas, pageNumber)
                }

                paint.color = if (item.isPass) Color.parseColor("#F0FDF4") else Color.parseColor("#FEF2F2")
                val rowRect = RectF(20f, currentY, pageWidth - 20f, currentY + 26f)
                canvas.drawRoundRect(rowRect, 4f, 4f, paint)

                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(item.date, 32f, currentY + 17f, paint)
                canvas.drawText(item.dayOfWeekHindi, 120f, currentY + 17f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(item.result, 220f, currentY + 17f, paint)

                paint.color = Color.parseColor("#D97706")
                canvas.drawText(item.otc.joinToString(" "), 320f, currentY + 17f, paint)

                if (item.isPass) {
                    paint.color = Color.parseColor("#16A34A")
                    canvas.drawText("PASS ✓", 480f, currentY + 17f, paint)
                } else {
                    paint.color = Color.parseColor("#DC2626")
                    canvas.drawText("FAIL ✗", 480f, currentY + 17f, paint)
                }

                currentY += 30f
            }

            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)

            val fileName = "Report_${marketName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val savedFile = savePdfToPublicDownloads(context, pdfDocument, fileName)
            pdfDocument.close()

            if (savedFile != null) {
                openOrSharePdfFile(context, savedFile)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF बनाने में समस्या: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Saves generated PDF document to phone's public "Downloads/A23_Reports" folder
     */
    private fun savePdfToPublicDownloads(context: Context, pdfDocument: PdfDocument, fileName: String): File? {
        return try {
            val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val a23Dir = File(publicDownloadsDir, "A23_Reports")
            if (!a23Dir.exists()) {
                a23Dir.mkdirs()
            }

            val targetFile = File(a23Dir, fileName)
            val fileOutputStream = FileOutputStream(targetFile)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Trigger MediaScanner so file managers instantly index the downloaded PDF in Downloads folder
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf("application/pdf"),
                null
            )

            Toast.makeText(
                context,
                "✅ PDF Complete History Downloaded to Downloads/A23_Reports/${targetFile.name}",
                Toast.LENGTH_LONG
            ).show()

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to app documents directory if public downloads throws permission exception
            val fallbackDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir, "A23_PDF_Reports")
            if (!fallbackDir.exists()) fallbackDir.mkdirs()
            val fallbackFile = File(fallbackDir, fileName)
            val fos = FileOutputStream(fallbackFile)
            pdfDocument.writeTo(fos)
            fos.close()

            Toast.makeText(context, "✅ PDF Report saved to: ${fallbackFile.name}", Toast.LENGTH_SHORT).show()
            fallbackFile
        }
    }

    private fun openOrSharePdfFile(context: Context, file: File) {
        val uri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val cacheFile = File(context.cacheDir, file.name)
                file.copyTo(cacheFile, overwrite = true)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    cacheFile
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "✅ PDF Report saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return
            }
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "A23 PDF रिपोर्ट खोलें"))
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "A23 PDF रिपोर्ट शेयर करें"))
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "✅ PDF Downloaded: Downloads/A23_Reports/${file.name}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

