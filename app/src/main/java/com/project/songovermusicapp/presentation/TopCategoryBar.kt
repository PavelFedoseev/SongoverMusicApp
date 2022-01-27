package com.project.songovermusicapp.presentation

import android.view.MotionEvent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.project.songovermusicapp.presentation.ui.theme.*
import com.project.songovermusicapp.presentation.ui.viewmodel.MainCategory

@ExperimentalComposeUiApi
@Composable
fun TopCategoryBar(
    modifier: Modifier = Modifier,
    categories: List<MainCategory>,
    selectedCategory: MainCategory,
    onCategorySelected: (MainCategory) -> Unit
) {

    val scrollState = rememberLazyListState()
    var selectedItemId = categories.indexOfFirst { it == selectedCategory }
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = TabCategoryPadding,
                end = TabCategoryPadding,
                bottom = TabCategoryPaddingVertical
            ), state = scrollState
    ) {
        items(items = categories, itemContent = { categoryItem ->
            val id = categories.indexOf(categoryItem)
            val isSelected = id == selectedItemId
            val size by animateFloatAsState(targetValue = if (isSelected) TabCategoryExpandedSize.value else TabCategoryCollapsedSize.value)
            Row(modifier = Modifier
                .height(
                    TabCategoryHeight
                )
                .pointerInteropFilter {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {

                        }

                        MotionEvent.ACTION_UP -> {
                            onCategorySelected(categories[id])
                            selectedItemId = id
                        }
                    }
                    true
                }
                , verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = TabCategoryPadding),
                    color = if (isSelected) WhiteExpandedIndicator else GrayCollapsedIndicator,
                    text = categories[id].name,
                    fontWeight = FontWeight.Bold,
                    fontSize = size.sp
                )
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun ComposablePreview() {

}