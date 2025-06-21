package com.richard.musicplayer.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// OTIMIZAÇÃO: Navegação ultra-simplificada para máxima performance
@Composable
fun ModernNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    tabs: List<NavigationTab>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp), // Reduzido de 80dp para 72dp
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f), // Mais opaco para melhor performance
        tonalElevation = 4.dp, // Reduzido de 8dp
        shadowElevation = 6.dp, // Reduzido de 12dp
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) // Reduzido de 24dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp), // Padding otimizado
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                OptimizedNavigationItem(
                    tab = tab,
                    selected = selectedTab == tab.route,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Haptic mais leve
                        onTabSelected(tab.route)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// OTIMIZAÇÃO: Item de navegação ultra-otimizado
@Composable
private fun OptimizedNavigationItem(
    tab: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // OTIMIZAÇÃO: Apenas animação de cor essencial
    val iconColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        animationSpec = tween(200), // Reduzido de 300ms
        label = "iconColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(200), // Reduzido de 300ms
        label = "textColor"
    )
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp)) // Reduzido de 16dp
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // OTIMIZAÇÃO: Background simplificado apenas se selecionado
        if (selected) {
            Box(
                modifier = Modifier
                    .size(48.dp) // Reduzido de 56dp
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), // Simplificado
                        shape = CircleShape
                    )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // OTIMIZAÇÃO: Ícone simplificado sem efeitos
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(22.dp), // Reduzido de 24dp
                tint = iconColor
            )
            
            Spacer(modifier = Modifier.height(2.dp)) // Reduzido de 4dp
            
            // OTIMIZAÇÃO: Texto simplificado
            Text(
                text = tab.label,
                fontSize = 11.sp, // Padronizado
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, // Simplificado
                color = textColor,
                maxLines = 1
            )
        }
    }
}

data class NavigationTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// OTIMIZAÇÃO: Função desnecessária removida para melhor performance 