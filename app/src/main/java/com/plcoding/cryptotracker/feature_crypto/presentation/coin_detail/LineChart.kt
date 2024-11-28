package com.plcoding.cryptotracker.feature_crypto.presentation.coin_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.cryptotracker.feature_crypto.domain.CoinPrice
import com.plcoding.cryptotracker.ui.theme.CryptoTrackerTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun LineChart(
    dataPoints: List<DataPoint>,
    style: ChartStyle,
    visibleDataPointIndices: IntRange,
    unit: String,
    modifier: Modifier = Modifier,
    selectedDataPoint: DataPoint? = null,
    onSelectDataPoint: (DataPoint) -> Unit = {},
    onXLabelWidthChange: (Float) -> Unit = {},
    showHelperLines: Boolean = true
) {
    val textStyle = LocalTextStyle.current.copy(
        fontSize = style.labelFontSize
    )

    val visibleDataPoints = remember(dataPoints, visibleDataPointIndices) {
        dataPoints.slice(visibleDataPointIndices)
    }

    var drawPoints by remember {
        mutableStateOf(listOf<DataPoint>())
    }

    val maxYValue = remember(visibleDataPoints) {
        visibleDataPoints.maxOfOrNull { it.y } ?: 0f
    }

    val minYValue = remember(visibleDataPoints) {
        visibleDataPoints.minOfOrNull { it.y } ?: 0f
    }

    val measurer = rememberTextMeasurer()

    var xLabelWidth by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(key1 = xLabelWidth) {
        onXLabelWidthChange(xLabelWidth)
    }

    val selectedDataPointIndex = remember(selectedDataPoint) {
        dataPoints.indexOf(selectedDataPoint)
    }

    var isShowingDataPoints by remember {
        mutableStateOf(selectedDataPoint != null)
    }

    Canvas(
        modifier = modifier.fillMaxSize()
            .pointerInput(drawPoints, xLabelWidth) {
                detectHorizontalDragGestures { change, _ ->
                    val newSelectedDataPoint = getSelectedDataPointIndex(
                        touchOffsetX = change.position.x,
                        triggerWidth = xLabelWidth,
                        drawPoints = drawPoints
                    )
                    val newRange = (newSelectedDataPoint + visibleDataPointIndices.first)
                    isShowingDataPoints = newRange in visibleDataPointIndices

                    if (isShowingDataPoints){
                        onSelectDataPoint(dataPoints[newSelectedDataPoint])
                    }
                }
            }
    ) {
        val minLabelSpacingYPx = style.minYLabelSpacing.toPx()
        val verticalPaddingPx = style.verticalPadding.toPx()
        val horizontalPaddingPx = style.horizontalPadding.toPx()
        val xAxisLabelSpacingPx = style.xAxisLabelSpacing.toPx()

        val xLabelTextLayoutResults = visibleDataPoints.map {
            measurer.measure(
                text = it.xLabel,
                style = textStyle.copy(textAlign = TextAlign.Center)
            )
        }

        val maxXLabelWidth = xLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0
        val maxXLabelHeight = xLabelTextLayoutResults.maxOfOrNull { it.size.height } ?: 0
        val maxXLabelLineCount = xLabelTextLayoutResults.maxOfOrNull { it.lineCount } ?: 0
        val xLabelLineHeight = maxXLabelHeight / maxXLabelLineCount

        val viewportHeightPx = size.height -
                (maxXLabelHeight + // highest red box
                        2 * verticalPaddingPx + // the red lines on top and bottom
                        xLabelLineHeight + // the blue box
                        xAxisLabelSpacingPx) // the cyan line

        // y label calculation

        val labelViewportHeightPx = viewportHeightPx + xLabelLineHeight
        val labelCountExcludingLast = ((labelViewportHeightPx / (xLabelLineHeight + minLabelSpacingYPx))).toInt()

        val valueIncrement  =  (maxYValue - minYValue)/labelCountExcludingLast

        val yLabels = (0..labelCountExcludingLast).map{
            ValueLabel(
                value = maxYValue - (valueIncrement * it),
                unit = unit
            )
        }

        val yLabelTextLayoutResults = yLabels.map{
            measurer.measure(
                text = it.format(),
                style = textStyle
            )
        }

        val maxYLabelWidth = yLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0

        val viewportTopY = verticalPaddingPx + xLabelLineHeight + 10f // 10 is static padding
        val viewportRightX = size.width
        val viewportBottomY = viewportTopY + viewportHeightPx
        val viewportLeftX = 2f * horizontalPaddingPx + maxYLabelWidth

        val viewport = Rect(
            left = viewportLeftX,
            top = viewportTopY,
            right = viewportRightX,
            bottom = viewportBottomY
        )

        xLabelWidth = maxXLabelWidth + xAxisLabelSpacingPx

        xLabelTextLayoutResults.forEachIndexed{ index, result ->
            val x = viewportLeftX + xAxisLabelSpacingPx / 2f + (xLabelWidth * index)
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = x,
                    y = viewportBottomY + xAxisLabelSpacingPx
                ),
                color = if(index == selectedDataPointIndex) style.selectedColor else style.unselectedColor
            )
            if (showHelperLines) {
                drawLine(
                    start = Offset(
                        x = x + result.size.width.toFloat()/2,
                        y = viewportTopY
                    ),
                     end = Offset(
                        x = x + result.size.width.toFloat()/2,
                        y = viewportBottomY
                     ),
                    color = if(index == selectedDataPointIndex) style.selectedColor else style.unselectedColor,
                    strokeWidth = if(index == selectedDataPointIndex) style.helperLinesThicknessPx * 2f else style.helperLinesThicknessPx
                )
            }

            if (selectedDataPointIndex == index) {
                val valueLabel = ValueLabel(
                    value = visibleDataPoints[index].y,
                    unit = "$"
                )

                val valueResult = measurer.measure(
                    text = valueLabel.format(),
                    style = textStyle.copy(
                        color = style.selectedColor
                    ),
                    maxLines = 1
                )

                val textPositionX = if (selectedDataPointIndex == visibleDataPointIndices.last) {
                    x - valueResult.size.width
                } else {
                    x - (valueResult.size.width / 2f)
                } + result.size.width / 2f

                val isTextInVisiblRange = (size.width - textPositionX).roundToInt() in 0..size.width.roundToInt()

                if (isTextInVisiblRange) {
                    drawText(
                        textLayoutResult = valueResult,
                        topLeft = Offset(
                            x = textPositionX,
                            y = viewportTopY - valueResult.size.height - 10f
                        )
                    )
                }
            }
        }


        val heightRequiredForLabels = xLabelLineHeight * (labelCountExcludingLast + 1)
        val remainingHeightForLabels = labelViewportHeightPx - heightRequiredForLabels
        val spaceBetweenLabels = remainingHeightForLabels / labelCountExcludingLast

        yLabelTextLayoutResults.forEachIndexed{ index, result ->
            val x = horizontalPaddingPx + maxYLabelWidth - result.size.width.toFloat()
            val y = viewportTopY +
                    index * (xLabelLineHeight + spaceBetweenLabels) -
                    xLabelLineHeight / 2f

            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = x,
                    y = y
                ),
                color = style.unselectedColor
            )

            if (showHelperLines) {
                drawLine(
                    color = style.unselectedColor,
                    start = Offset(
                        x = viewportLeftX,
                        y = y + (result.size.height.toFloat()/2)
                    ),
                    end = Offset(
                        x = viewportRightX,
                        y = y + (result.size.height.toFloat()/2)
                    ),
                    strokeWidth = style.helperLinesThicknessPx
                )
            }
        }

        drawPoints = visibleDataPointIndices.map{
            val x = viewportLeftX + (it - visibleDataPointIndices.first) * xLabelWidth + xLabelWidth/2f
            val ratio = (dataPoints[it].y - minYValue) / (maxYValue - minYValue)
            val y = viewportBottomY - (ratio * viewportHeightPx)
            DataPoint(x,y,dataPoints[it].xLabel)
        }

        val cPts1 = mutableListOf<DataPoint>()
        val cPts2 = mutableListOf<DataPoint>()

        for (i in 1 until drawPoints.size){
            val p0 = drawPoints[i-1]
            val p1 = drawPoints[i]

            val x = (p1.x + p0.x)/2f
            val y1 = p0.y
            val y2 = p1.y

            cPts1.add(DataPoint(x,y1,""))
            cPts2.add(DataPoint(x,y2,""))
        }

        val linePath = Path().apply{
            if (drawPoints.isNotEmpty()) {
                moveTo(drawPoints.first().x, drawPoints.first().y)

                for (i in 1 until drawPoints.size) {
                    cubicTo(
                        x1 = cPts1[i-1].x,
                        y1 = cPts1[i-1].y,
                        x2 = cPts2[i-1].x,
                        y2 = cPts2[i-1].y,
                        x3 = drawPoints[i].x,
                        y3 = drawPoints[i].y,
                    )
                }
            }
        }

        drawPath(
            path = linePath,
            color = style.lineColor,
            style = Stroke(
                width = 5f,
                cap = StrokeCap.Round
            )
        )

        drawPoints.forEachIndexed { index, point ->
            if (isShowingDataPoints) {
                val circleOffset =  Offset(
                    point.x,
                    point.y
                )
                drawCircle(
                    color = style.lineColor,
                    radius = 10f,
                    center = circleOffset
                )
                if (selectedDataPointIndex == index) {
                    drawCircle(
                        color = Color.White,
                        radius = 15f,
                        center = circleOffset
                    )
                    drawCircle(
                        color = style.lineColor,
                        radius = 15f,
                        center = circleOffset,
                        style = Stroke(
                            width = 3f
                        )

                    )
                }
            }
        }

    }

}

private fun getSelectedDataPointIndex(
    touchOffsetX: Float,
    triggerWidth: Float,
    drawPoints: List<DataPoint>
): Int {
    val triggerRangeLeft = touchOffsetX - triggerWidth / 2f
    val triggerRangeRight = touchOffsetX + triggerWidth / 2f
    return drawPoints.indexOfFirst {
        it.x in triggerRangeLeft..triggerRangeRight
    }
}

@Preview
@Composable
private fun LineChartPreview() {
    CryptoTrackerTheme {
        val coinHistoryRandom = remember{
            (1 .. 20).map {
                CoinPrice(
                    priceUsd = Random.nextFloat() * 1000.0,
                    dateTime = ZonedDateTime.now().plusHours(it.toLong())
                )
            }
        }

        val style = ChartStyle(
            lineColor = Color.Black,
            unselectedColor = Color(0xFF7C7C7C),
            selectedColor = Color.Black,
            helperLinesThicknessPx = 1f,
            axisLinesThicknessPx = 5f,
            labelFontSize = 14.sp,
            minYLabelSpacing = 25.dp,
            verticalPadding = 8.dp,
            horizontalPadding = 8.dp,
            xAxisLabelSpacing = 8.dp
        )

        val dataPoints = remember {
            coinHistoryRandom.map{
                DataPoint(
                    x = it.dateTime.hour.toFloat(),
                    y = it.priceUsd.toFloat(),
                    xLabel = DateTimeFormatter
                        .ofPattern("ha\nM/d")
                        .format(it.dateTime)
                )
            }
        }
        
        LineChart(
            dataPoints = dataPoints,
            style = style,
            visibleDataPointIndices = 0..19,
            unit = "$",
            modifier = Modifier
                .width(700.dp)
                .height(300.dp)
                .background(Color.White)
            ,
            selectedDataPoint = dataPoints[1]
        )
    }
}