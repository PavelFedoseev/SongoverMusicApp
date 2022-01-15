package com.project.songovermusicapp.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.project.songovermusicapp.presentation.ui.theme.TabCategoryCollapsedSize
import com.project.songovermusicapp.presentation.ui.theme.TabCategoryExpandedSize

@Composable
fun TopBarLazyColumn(listOfCategories: List<String>){
    val scrollState = rememberScrollState()
    LazyColumn(modifier = Modifier.horizontalScroll(scrollState)){
        items(items = listOfCategories, itemContent = {
            for(i in 0..listOfCategories.size) {
                val isSelected = false
                Box() {
                    Text(
                        text = listOfCategories[i],
                        fontSize = if (isSelected) TabCategoryExpandedSize else TabCategoryCollapsedSize
                    )
                }
            }
        })
    }
}

@Composable
fun TopBarCategoryItem(name: String, isSelected: Boolean){

}

@Preview(showBackground = true)
@Composable
fun ComposablePreview(){

}