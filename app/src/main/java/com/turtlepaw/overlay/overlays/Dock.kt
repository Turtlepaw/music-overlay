package com.turtlepaw.overlay.overlays

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.skydoves.cloudy.cloudy
import com.turtlepaw.overlay.MediaInfo
import com.turtlepaw.overlay.components.TimeText
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun OverlayDock(data: MediaInfo?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(bottom = 15.dp),
        colors = SurfaceDefaults.colors(
            containerColor = Color.Black,
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        if (data?.albumArtUri != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data.albumArtUri)
                    .crossfade(true) // Optional: for smooth loading
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .cloudy(
                        radius = 250
                    )
                    .alpha(0.5f),
                contentScale = ContentScale.Crop
            ) {
                val state by painter.state.collectAsState()
                if (state is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(15.dp)
                ).height(75.dp),
            colors = SurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.15f),
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Log.d("AlbumArtUri", "AlbumArtUri: ${data?.albumArtUri}")

                // get the time in HH:MM format
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm"))

                TimeText(
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 34.sp,
                    )
                )

                Text(
                    text = "•",
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 35.sp,
                    ),
                    modifier = Modifier.padding(horizontal = 15.dp)
                )

                // show the image from the url
                if (data?.albumArtUri != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.albumArtUri)
                            .crossfade(true) // Optional: for smooth loading
                            .build(),
                        contentDescription = null
                    ) {
                        val state by painter.state.collectAsState()
                        if (state is AsyncImagePainter.State.Success) {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier
                                    .width(
                                        50.dp
                                    ).height(50.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.FillBounds
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(0.2f), RoundedCornerShape(8.dp))
                                    .size(
                                        43.dp
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(15.dp))
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = data?.title ?: "Nothing playing",
                        color = Color.White,
                        style = (if (data?.title == null) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleLarge).copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = 4f
                            )
                        ),
                    )
                    Text(
                        text = data?.artist ?: "",
                        color = Color.White.copy(0.63f),
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}