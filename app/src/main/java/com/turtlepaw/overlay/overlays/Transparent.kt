package com.turtlepaw.overlay.overlays

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.turtlepaw.overlay.GoogleSans
import com.turtlepaw.overlay.MediaInfo
import com.turtlepaw.overlay.components.TimeText

@Composable
fun OverlayTransparent(data: MediaInfo?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(0.2f),
                        Color.Black.copy(0.45f),
                        Color.Black.copy(0.6f),
                        Color.Black.copy(0.8f)
                    ),
                )
            )
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 15.dp),
        colors = SurfaceDefaults.colors(
            containerColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            colors = SurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0f),
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                TimeText(
                    textAlign = TextAlign.Start,
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 60.sp,
                        fontWeight = FontWeight.W400,
                        fontFamily = GoogleSans
                    )
                )

                Row() {
                    if (data?.title == null) {
                        Icon(
                            imageVector = Icons.Rounded.MusicOff,
                            contentDescription = "Music Off",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Nothing playing",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.W400,
                                fontSize = 25.sp,
                                fontFamily = GoogleSans
                            )
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                text = data?.title ?: "Nothing playing",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.W400,
                                    fontSize = 28.sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        blurRadius = 4f
                                    ),
                                    fontFamily = GoogleSans
                                ),
                            )
                            if(data?.artist != null) {
                                Text(
                                    text = data?.artist ?: "",
                                    color = Color.White.copy(0.8f),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.W400,
                                        fontSize = 23.sp,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            blurRadius = 4f
                                        ),
                                        fontFamily = GoogleSans
                                    ),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(15.dp))

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
                                        .clip(RoundedCornerShape(8.dp))
                                        .size(
                                            60.dp
                                        ),
                                    contentScale = ContentScale.FillBounds
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(0.2f), RoundedCornerShape(8.dp))
                                        .size(
                                            55.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun OverlayTransparentPreview(){
    OverlayTransparent(
        data = MediaInfo(
            title = "Hello",
            artist = "World",
            albumArtUri = "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
        )
    )
}
