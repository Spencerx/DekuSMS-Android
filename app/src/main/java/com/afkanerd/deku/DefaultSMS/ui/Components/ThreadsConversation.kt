package com.afkanerd.deku.DefaultSMS.ui.Components

import android.content.Context
import android.provider.Telephony
import com.afkanerd.deku.DefaultSMS.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import coil3.compose.AsyncImage
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Extensions.toHslColor
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import java.nio.file.WatchEvent

@Preview
@Composable
fun DeleteConfirmationAlert(
    confirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.secondary,
        title = {
            Text(
                stringResource(R.string.messages_thread_delete_confirmation_title),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.messages_thread_delete_confirmation_text),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {
            TextButton(
                onClick = { confirmCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.messages_thread_delete_confirmation_yes),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    )
}

@Preview
@Composable
fun ImportDetails(
    numOfConversations: Int = 0,
    numOfThreads: Int = 0,
    confirmCallback: (() -> Unit)? = null,
    resetConfirmCallback: (() -> Unit)? = null,
    dismissCallback: (() -> Unit)? = null,
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.secondary,
        title = {
            Text(
                stringResource(R.string.import_conversations),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.threads) + numOfThreads,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    stringResource(R.string.conversations) + numOfConversations,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
        onDismissRequest = { dismissCallback?.invoke() },
        confirmButton = {
            if(BuildConfig.DEBUG)
                TextButton(
                    onClick = {resetConfirmCallback?.invoke()}
                ) {
                    Text(
                        stringResource(R.string.reset_and_import),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

            TextButton(
                onClick = { confirmCallback?.invoke() }
            ) {
                Text(
                    stringResource(R.string.conversation_menu_import),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismissCallback?.invoke() }
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    )
}

@Composable
private fun ThreadConversationsAvatar(
    context: Context,
    id: String,
    firstName: String,
    lastName: String,
    phoneNumber: String,
    isContact: Boolean = true) {


    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        if (isContact) {
            val contactPhotoUri = remember(phoneNumber) {
                Contacts.retrieveContactPhoto(context, phoneNumber)
            }
            if (contactPhotoUri.isNotEmpty() && contactPhotoUri != "null") {
                AsyncImage(
                    model = contactPhotoUri,
                    contentDescription = "Contact Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                val color = remember(id, firstName, lastName) {
                    Color("$id / $firstName".toHslColor())
                }
                val initials = (firstName.take(1) + lastName.take(1)).uppercase()
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(SolidColor(color))
                }
                Text(text = initials, style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = "",
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(10.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ThreadConversationCard(
    phoneNumber: String = "0612345678",
    id: String = "id",
    firstName: String = "Jane",
    lastName: String = "",
    content: String = "Text Template",
    date: String = "Tues",
    isRead: Boolean = false,
    isContact: Boolean = false,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isMuted: Boolean = LocalInspectionMode.current,
    isBlocked: Boolean = LocalInspectionMode.current,
    type: Int? = if(LocalInspectionMode.current)
        Telephony.Sms.MESSAGE_TYPE_FAILED else null
) {
    var weight = FontWeight.Bold
    val colorHeadline = when {
        isRead || isBlocked -> {
            weight = FontWeight.Normal
            MaterialTheme.colorScheme.secondary
        }
        else -> MaterialTheme.colorScheme.onBackground
    }
    val colorContent = when(type) {
        Telephony.Sms.MESSAGE_TYPE_FAILED ->
            MaterialTheme.colorScheme.error
        Telephony.Sms.MESSAGE_TYPE_OUTBOX -> MaterialTheme.colorScheme.secondary
        else -> colorHeadline
    }

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Row {
                Text(
                    text = "$firstName $lastName",
                    color = colorHeadline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = weight
                )

                if(isMuted)
                    Icon(Icons.AutoMirrored.Default.VolumeOff, stringResource(R.string.thread_muted))

                if(isBlocked)
                    Icon(Icons.Filled.Block, stringResource(R.string.contact_is_blocked))
            }
        },
        supportingContent = {
            Text(
                text = when(type) {
                    Telephony.Sms.MESSAGE_TYPE_DRAFT ->
                        stringResource(R.string.thread_conversation_type_draft) + ": $content"
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX ->
                        stringResource(R.string.sms_status_sending)+ ": $content"
                    Telephony.Sms.MESSAGE_TYPE_FAILED ->
                        stringResource(R.string.sms_status_failed_only)+ ": $content"
                    else -> content
                },
                color = colorContent,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = if(
                    type == Telephony.Sms.MESSAGE_TYPE_DRAFT ||
                    type == Telephony.Sms.MESSAGE_TYPE_OUTBOX ||
                    type == Telephony.Sms.MESSAGE_TYPE_FAILED
                    ) FontStyle.Italic else null,
                fontWeight = weight,
                maxLines = if(isRead) 1 else 3,
            )
        },
        trailingContent = {
            Text(
                text = date,
                color = colorContent,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = weight,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        },
        leadingContent = {
            ThreadConversationsAvatar(LocalContext.current, id, firstName, lastName, phoneNumber, isContact)
        }
    )
}
