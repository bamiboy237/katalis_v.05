package com.katalis.app.presentation.components.chat

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.katalis.app.presentation.theme.KatalisTheme
import java.io.InputStream

@Composable
fun EnhancedChatInput(
    onSendTextMessage: (String) -> Unit,
    onSendMultimodalMessage: (String, Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type your message...",
    isEnabled: Boolean = true
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    // Simplified permission handling without Accompanist for now
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri ->
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null && textFieldValue.text.isNotBlank()) {
                    onSendMultimodalMessage(textFieldValue.text, bitmap)
                    textFieldValue = TextFieldValue("")
                }
            } catch (_: Exception) {
                // Handle error - could show a toast or error message
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            imagePickerLauncher.launch("image/*")
        } else {
            showPermissionDialog = true
        }
    }

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Camera/Image button - simplified for now
            IconButton(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                },
                enabled = isEnabled,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Add image",
                    tint = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input field
            BasicTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                enabled = isEnabled,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            IconButton(
                onClick = {
                    if (textFieldValue.text.isNotBlank()) {
                        onSendTextMessage(textFieldValue.text)
                        textFieldValue = TextFieldValue("")
                    }
                },
                enabled = isEnabled && textFieldValue.text.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled && textFieldValue.text.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (isEnabled && textFieldValue.text.isNotBlank()) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }

    // Permission rationale dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Needed") },
            text = { 
                Text("To share images with the AI tutor, camera permission is required to access your photo library.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EnhancedChatInputPreview() {
    KatalisTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            EnhancedChatInput(
                onSendTextMessage = { },
                onSendMultimodalMessage = { _, _ -> },
                placeholder = "Ask me anything about Physics, Math, or Medicine..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            EnhancedChatInput(
                onSendTextMessage = { },
                onSendMultimodalMessage = { _, _ -> },
                placeholder = "AI is thinking...",
                isEnabled = false
            )
        }
    }
}