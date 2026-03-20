package com.andeshub.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andeshub.R
import com.andeshub.ui.theme.Yellow
import com.andeshub.ui.theme.SoftCream
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val color: Color,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to AndesHub",
            description = "The exclusive marketplace for Los Andes students.",
            color = Color(0xFFFDEBB2),
            imageRes = R.drawable.welcome_on
        ),
        OnboardingPage(
            title = "Safe & Secure",
            description = "Verify your student email and trade safely with your classmates.",
            color = Color(0xFFFDEBB2),
            imageRes = R.drawable.safe_on
        ),
        OnboardingPage(
            title = "Start Trading",
            description = "Buy or sell books, tech, and more. Join us today!",
            color = Color(0xFFFDEBB2),
            imageRes = R.drawable.start_on
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { position ->
            OnboardingPageContent(page = pages[position])
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == iteration) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                ) {
                    Text(
                        text = "Back",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(page.color),  // ← intencional, es el color de la página
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}