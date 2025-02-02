package com.turtlepaw.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.skydoves.cloudy.cloudy

@Composable
fun PreviewBox(height: Dp = 200.dp, content: @Composable () -> Unit){
    Box(
        modifier = Modifier.height(height).padding(horizontal = 16.dp).padding(bottom = 16.dp).border(
            width = 1.5.dp,
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(15.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.plants),
            contentDescription = "Plants",
            modifier = Modifier.clip(
                RoundedCornerShape(15.dp)
            ).cloudy(
                radius = 1000
            )
                .fillMaxSize()
            ,
            contentScale = ContentScale.FillWidth
        )
        content()
    }
}

@Composable
fun InterfacePreview(cardWidth: Dp, uiMode: UiMode, onBack: () -> Unit, onUiModeSelected: (UiMode) -> Unit) {
    val time = "12:30pm"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            18.dp,
            Alignment.CenterVertically
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack){
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        Text("UI Mode", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Card(
            modifier = Modifier.width(cardWidth),
            onClick = {
                onUiModeSelected(UiMode.Transparent)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Transparent",
                )

                if(uiMode == UiMode.Transparent){
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Check",
                        tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            PreviewBox(height = 120.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(0.2f),
                                    Color.Black.copy(0.45f),
                                    Color.Black.copy(0.6f),
                                    Color.Black.copy(0.8f)
                                ),
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .padding(16.dp)
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxSize().padding(
                            horizontal = 6.dp,
                        ).padding(bottom = 5.dp)
                    ){
                        Text(
                            text = time,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.W400
                            )
                        )

                        Row {
                            Icon(
                                imageVector = Icons.Rounded.MusicOff,
                                contentDescription = "Music Off",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Nothing playing",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.W400,
                                    fontSize = 23.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.width(cardWidth),
            onClick = {
                onUiModeSelected(UiMode.Dock)
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Dock",
                )

                if(uiMode == UiMode.Dock){
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Check",
                        tint = Color.White.copy(0.8f),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            PreviewBox(height = 120.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(15.dp)
                        )
                ){
                    Image(
                        painter = painterResource(R.drawable.flower),
                        contentDescription = "Flower",
                        modifier = Modifier.matchParentSize()
                            .clip(
                                RoundedCornerShape(15.dp)
                            )
                            .fillMaxWidth()
                            .cloudy(
                                radius = 2700
                            ),
                        contentScale = ContentScale.FillWidth
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            text = time,
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.W400
                            )
                        )

                        Text(
                            text = "•",
                            color = Color.White.copy(0.7f),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 30.sp,
                            ),
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )

                        Row {
                            Icon(
                                imageVector = Icons.Rounded.MusicOff,
                                contentDescription = "Music Off",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(modifier = Modifier.size(10.dp))
                            Text(
                                text = "Nothing playing",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.W400,
                                    fontSize = 23.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}