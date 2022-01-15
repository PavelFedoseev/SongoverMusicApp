package com.project.songovermusicapp.presentation

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
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.project.songovermusicapp.presentation.ui.theme.*
import com.project.songovermusicapp.presentation.ui.viewmodel.MainCategory

@Composable
fun TopBarLazyColumn(
    modifier: Modifier = Modifier,
    categories: List<MainCategory>,
    selectedCategory: MainCategory,
    onCategorySelected: (MainCategory) -> Unit
) {
    val scrollState = rememberLazyListState()
    var selectedItemId by remember { mutableStateOf(0) }
    LazyRow(modifier = modifier.fillMaxWidth().padding(horizontal = TabCategoryPadding), state = scrollState) {
        items(items = categories, itemContent = { categoryItem ->
            val id = categories.indexOf(categoryItem)
            val isSelected = id == selectedItemId
            val size by animateFloatAsState(targetValue = if (isSelected) TabCategoryExpandedSize.value else TabCategoryCollapsedSize.value)
            Row(modifier = Modifier
                .padding(horizontal = TabCategoryPadding)
                .height(
                    TabCategoryHeight
                )
                .clickable {
                    onCategorySelected(categories[id])
                    selectedItemId = id
                }, verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    color = if (isSelected) WhiteExpandedIndicator else GrayCollapsedIndicator,
                    text = categories[id].name,
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