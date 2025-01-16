package com.afkanerd.deku.DefaultSMS.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.expandIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ListItem
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.InsertComment
import androidx.compose.material.icons.automirrored.twotone.InsertComment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.RemoteInput
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import androidx.room.util.TableInfo
import com.afkanerd.deku.Datastore
import com.afkanerd.deku.DefaultSMS.AboutActivity
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.BuildConfig
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.DefaultSMS.ComposeNewMessageScreen
import com.afkanerd.deku.DefaultSMS.ConversationsScreen
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.HomeScreen
import com.afkanerd.deku.DefaultSMS.Models.Archive
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversations
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler
import com.afkanerd.deku.DefaultSMS.Models.ExportImportHandlers
import com.afkanerd.deku.DefaultSMS.Models.Notifications
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.ThreadsCount
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.DefaultSMS.SearchThreadScreen
import com.afkanerd.deku.DefaultSMS.SettingsActivity
import com.afkanerd.deku.DefaultSMS.ui.Components.ConversationStatusTypes
import com.afkanerd.deku.DefaultSMS.ui.Components.DeleteConfirmationAlert
import com.afkanerd.deku.DefaultSMS.ui.Components.ImportDetails
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.afkanerd.deku.Router.GatewayServers.GatewayServerRoutedActivity
import com.example.compose.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.exp

enum class InboxType(val value: Int) {
    INBOX(0),
    ARCHIVED(1),
    ENCRYPTED(2),
    BLOCKED(3),
    DRAFTS(4),
    MUTED(5);

    companion object {
        fun fromInt(value: Int): InboxType? {
            return InboxType.entries.find { it.value == value }
        }
    }
}

@Composable
fun SwipeToDeleteBackground(
    dismissState: SwipeToDismissBoxState? = null,
    inArchive: Boolean = false
) {
    var arrangement = Arrangement.End
    val color = when(dismissState?.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> {
            arrangement = Arrangement.Start
            MaterialTheme.colorScheme.error
        }
        SwipeToDismissBoxValue.EndToStart -> {
            MaterialTheme.colorScheme.primary
        }
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = arrangement
    ) {
        Icon(
            when(dismissState?.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                else -> {
                    when {
                        inArchive -> Icons.Default.Unarchive
                        else -> Icons.Default.Archive
                    }
                }
            },
            tint = MaterialTheme.colorScheme.onPrimary,
            contentDescription = stringResource(R.string.messages_threads_menu_archive)
        )
    }
}

fun processIntents(
    context: Context,
    intent: Intent,
    defaultRegion: String,
): Triple<String?, String?, String?>?{
    if(intent.action != null &&
        ((intent.action == Intent.ACTION_SENDTO) || (intent.action == Intent.ACTION_SEND))) {
        val text = if(intent.hasExtra("sms_body")) intent.getStringExtra("sms_body")
        else if(intent.hasExtra("android.intent.extra.TEXT")) {
            intent.getStringExtra("android.intent.extra.TEXT")
        } else ""

        val sendToString = intent.dataString

        if ((
                    sendToString != null &&
                            (sendToString.contains("smsto:") ||
                                    sendToString.contains("sms:"))) ||
            intent.hasExtra("address")
        ) {

            val address = Helpers.getFormatCompleteNumber(
                if(intent.hasExtra("address")) intent.getStringExtra("address")
                else sendToString, defaultRegion)

            val threadId =
                ThreadedConversationsHandler.get(context, address)
                    .thread_id
            return Triple(address, threadId, text)
        }
    }
    else if(intent.hasExtra("address")) {
        var text = if(intent.hasExtra("android.intent.extra.TEXT"))
            intent.getStringExtra("android.intent.extra.TEXT") else ""

        val address = intent.getStringExtra("address")
        val threadId = intent.getStringExtra("thread_id")
        return Triple(address, threadId, text)
    }
    return null
}

fun navigateToConversation(
    conversationsViewModel: ConversationsViewModel,
    address: String,
    threadId: String,
    subscriptionId: Int?,
    navController: NavController,
    searchQuery: String? = ""
) {
    conversationsViewModel.address = address
    conversationsViewModel.threadId = threadId
    conversationsViewModel.contactName = ""
    conversationsViewModel.searchQuery = searchQuery ?: ""
    conversationsViewModel.subscriptionId = subscriptionId ?: -1
    conversationsViewModel.liveData = null
    if(conversationsViewModel.newLayoutInfo?.displayFeatures!!.isEmpty())
        navController.navigate(ConversationsScreen)
}

@Preview(showBackground = true)
@Composable
fun ModalDrawerSheetLayout(
    callback: ((InboxType) -> Unit)? = null,
    selectedItemIndex: InboxType = InboxType.INBOX,
    counts: ThreadsCount? = null,
) {
    ModalDrawerSheet {
        Text(
            stringResource(R.string.folders),
            fontSize = 12.sp,
            modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Inbox,
                        contentDescription = stringResource(R.string.inbox_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_inbox ),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.unreadCount > 0)
                            Text(counts.unreadCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.INBOX,
                onClick = { callback?.let{ it(InboxType.INBOX) } }
            )
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Archive,
                        contentDescription = stringResource(R.string.archive_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_archived ),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.archivedCount > 0)
                            Text(counts.archivedCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.ARCHIVED,
                onClick = { callback?.let{ it(InboxType.ARCHIVED) } }
            )
            HorizontalDivider()
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Drafts,
                        contentDescription = stringResource(R.string.thread_conversation_type_draft)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_drafts),
                        fontSize = 14.sp
                    )
                },
                badge = {
                    counts?.let {
                        if(counts.draftsCount > 0)
                            Text(counts.draftsCount.toString(), fontSize = 14.sp)
                    }
                },
                selected = selectedItemIndex == InboxType.DRAFTS,
                onClick = { callback?.let{ it(InboxType.DRAFTS) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = stringResource(R.string.encrypted_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_encryption),
                        fontSize = 14.sp
                    )
                },
                badge = {
                },
                selected = selectedItemIndex == InboxType.ENCRYPTED,
                onClick = { callback?.let{ it(InboxType.ENCRYPTED) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Default.VolumeOff,
                        contentDescription = stringResource(R.string.conversation_menu_muted_label)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversation_menu_muted_label),
                        fontSize = 14.sp
                    )
                },
                badge = {
                },
                selected = selectedItemIndex == InboxType.MUTED,
                onClick = { callback?.let{ it(InboxType.MUTED) } }
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Block,
                        contentDescription = stringResource(R.string.blocked_folder)
                    )
                },
                label = {
                    Text(
                        stringResource(R.string.conversations_navigation_view_blocked),
                        fontSize = 14.sp
                    )
                },
                 badge = {
                },
                selected = selectedItemIndex == InboxType.BLOCKED,
                onClick = { callback?.let{ it(InboxType.BLOCKED) } }
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainDropDownMenu(
    expanded: Boolean = false,
    importCallback: (() -> Unit)? = null,
    dismissCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { dismissCallback?.let{ it(false) } },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.homepage_menu_routed),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, GatewayServerRoutedActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.settings_title),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.conversation_menu_export),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    ExportImportHandlers.exportInbox(context)
                }
            )

            if(BuildConfig.DEBUG)
                DropdownMenuItem(
                    text = {
                        Text(
                            text= stringResource(R.string.conversation_menu_import),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        dismissCallback?.let { it(false) }
//                        ExportImportHandlers.importInbox(context)
                        importCallback?.invoke()
                    }
                )

            DropdownMenuItem(
                text = {
                    Text(
                        text=stringResource(R.string.about_deku),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                onClick = {
                    dismissCallback?.let { it(false) }
                    context.startActivity(
                        Intent(context, AboutActivity::class.java).apply {
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ThreadConversationLayout(
    conversationsViewModel: ConversationsViewModel = ConversationsViewModel(),
    intent: Intent? = null,
    navController: NavController,
    _items: List<Conversation> = emptyList(),
) {

    val inPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current

    intent?.let {
//        conversationsViewModel.text = ""
        val defaultRegion = if(inPreviewMode) "cm" else Helpers.getUserCountry(context)
        processIntents(context, intent, defaultRegion)?.let {
            intent.apply {
                removeExtra("address")
                removeExtra("thread_id")
                removeExtra("sms_body")
                removeExtra("android.intent.extra.TEXT")
                data = null
            }
            it.first?.let{ address ->
                it.second?.let { threadId ->
                    it.third?.let{ message ->
                        conversationsViewModel.text = message
                    }
                    navigateToConversation(
                        conversationsViewModel = conversationsViewModel,
                        address = address,
                        threadId = threadId,
                        subscriptionId = SIMHandler.getDefaultSimSubscription(context),
                        navController = navController,
                    )
                }
            }
        }
    }

    val counts by conversationsViewModel.getCount(context).observeAsState(null)

    var inboxType by remember { mutableStateOf(conversationsViewModel.inboxType) }

    val items: List<Conversation> by conversationsViewModel
        .getThreading(context).observeAsState(emptyList())

    val archivedItems: List<Conversation> by conversationsViewModel
        .archivedLiveData!!.observeAsState(emptyList())

    val mutedItems: List<Conversation> by conversationsViewModel
        .mutedLiveData!!.observeAsState(emptyList())

    var blockedItems: MutableList<Conversation> = remember { mutableStateListOf() }
    var encryptedItems: MutableList<Conversation> = remember { mutableStateListOf() }

    val draftsItems: List<Conversation> by conversationsViewModel
        .draftsLiveData!!.observeAsState(emptyList())

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var selectedItems = remember { mutableStateListOf<Conversation>() }
    var slideDeleteItem = remember { mutableStateOf("") }

    val selectedIconColors = MaterialTheme.colorScheme.primary
    var selectedItemIndex by remember { mutableStateOf(conversationsViewModel.inboxType) }

    var rememberMenuExpanded by remember { mutableStateOf( false)}
    var rememberImportMenuExpanded by remember { mutableStateOf( false)}
    var rememberDeleteMenu by remember { mutableStateOf( false)}
    var rememberItemsIsEmpty by remember { mutableStateOf(false)}
    var rememberArchivedIsEmpty by remember { mutableStateOf(false)}

    val scope = rememberCoroutineScope()
    val coroutineScope = remember { CoroutineScope(Dispatchers.Default) }

    LaunchedEffect(items) {
        rememberItemsIsEmpty = items.isEmpty()
    }

    LaunchedEffect(archivedItems) {
        rememberArchivedIsEmpty = archivedItems.isEmpty()
    }

    LaunchedEffect(inboxType) {
        if(inboxType == InboxType.BLOCKED) {
            coroutineScope.launch {
                items.forEach {
                    if(BlockedNumberContract.isBlocked(context, it.address)) blockedItems.add(it)
                }
            }
        }
    }

    BackHandler {
        if(conversationsViewModel.inboxType != InboxType.INBOX) {
            conversationsViewModel.inboxType = InboxType.INBOX
            selectedItemIndex = InboxType.INBOX
            inboxType = InboxType.INBOX
        }
        else if(!selectedItems.isEmpty()) {
            selectedItems.clear()
        }
        else {
            if(context is AppCompatActivity) {
                context.finish()
            }
        }
    }

    LaunchedEffect(conversationsViewModel.importDetails) {
        rememberImportMenuExpanded = conversationsViewModel.importDetails.isNotBlank()
    }

    MainDropDownMenu(
        expanded=rememberMenuExpanded,
        importCallback = {
            ExportImportHandlers.importInbox(context)
        }
    ) {
        rememberMenuExpanded = it
    }

    ModalNavigationDrawer(
        modifier = Modifier.safeDrawingPadding(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheetLayout(
                callback = { type ->
                    scope.launch {
                        drawerState.apply {
                            if(isClosed) open() else close()
                            inboxType = type
                            selectedItemIndex = type
                            conversationsViewModel.inboxType = type
                        }
                    }
                },
                selectedItemIndex = selectedItemIndex,
                counts = counts,
            )
        },
    ) {
        Scaffold (
            modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
            topBar = {
                if(inboxType == InboxType.INBOX && selectedItems.isEmpty()) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text= stringResource(R.string.app_name),
                                maxLines =1,
                                overflow = TextOverflow.Ellipsis)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if(isClosed) { open() }
                                        else { close() }
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(R.string.open_side_menu)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate(SearchThreadScreen)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(R.string.search_messages)
                                )
                            }
                            IconButton(onClick = {
                                rememberMenuExpanded = !rememberMenuExpanded
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.open_menu)
                                )
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
                else if(!selectedItems.isEmpty()) {
                    TopAppBar(
                        title = {
                            Text(
                                text= "${selectedItems.size} ${stringResource(R.string.selected)}",
                                maxLines =1,
                                color = selectedIconColors,
                                overflow = TextOverflow.Ellipsis)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                selectedItems.clear()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    tint = selectedIconColors,
                                    contentDescription = stringResource(R.string.cancel_selection)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if(inboxType == InboxType.ARCHIVED) {
                                    coroutineScope.launch {
                                        val threads : List<String> = selectedItems.map{
                                            it.thread_id!!
                                        }
                                        conversationsViewModel.unArchive(context, threads)
                                        selectedItems.clear()
                                    }
                                } else {
                                    coroutineScope.launch {
                                        val threads : List<String> = selectedItems.map{
                                            it.thread_id!!
                                        }
                                        conversationsViewModel.archive(context, threads)
                                        selectedItems.clear()
                                    }
                                }
                            }) {
                                if(inboxType == InboxType.ARCHIVED) {
                                    Icon(
                                        imageVector = Icons.Filled.Unarchive,
                                        tint = selectedIconColors,
                                        contentDescription =
                                        stringResource(R.string.unarchive_messages)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Archive,
                                        tint = selectedIconColors,
                                        contentDescription =
                                        stringResource(R.string.messages_threads_menu_archive)
                                    )
                                }
                            }

                            IconButton(onClick = {
                                rememberDeleteMenu = true
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    tint = selectedIconColors,
                                    contentDescription =
                                    stringResource(R.string.message_threads_menu_delete)
                                )
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
                else {
                    TopAppBar(
                        title = {
                            Text(
                                text= when(inboxType) {
                                    InboxType.ARCHIVED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_archived)
                                    InboxType.ENCRYPTED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_encryption)
                                    InboxType.BLOCKED ->
                                        stringResource(R.string
                                            .conversations_navigation_view_blocked)
                                    InboxType.MUTED ->
                                        stringResource(R.string
                                            .conversation_menu_muted_label)
                                    InboxType.DRAFTS ->
                                        stringResource(R.string
                                            .conversations_navigation_view_drafts)
                                    else -> ""
                                },
                                maxLines =1,
                                overflow = TextOverflow.Ellipsis)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if(isClosed) open() else close()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(R.string.open_side_menu)
                                )
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate(ComposeNewMessageScreen)
                    },
                    icon = { Icon( Icons.AutoMirrored.Default.Message,
                        stringResource(R.string.compose_new_message)) },
                    text = { Text(text = stringResource(R.string.compose)) },
                    expanded = listState.isScrollingUp()
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {

                when(inboxType) {
                    InboxType.INBOX -> {
                        if(items.isEmpty())
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.homepage_no_message),
                                    fontSize = 24.sp
                                )
                            }
                    }
                    InboxType.ARCHIVED -> {
                        if(archivedItems.isEmpty())
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    stringResource(R.string.homepage_archive_no_message),
                                    fontSize = 24.sp
                                )
                            }
                    }
                    InboxType.ENCRYPTED -> {}
                    InboxType.BLOCKED -> {}
                    InboxType.DRAFTS -> {}
                    InboxType.MUTED -> {}
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                )  {
                    itemsIndexed(
                        items = if(inPreviewMode) _items else when(inboxType) {
                            InboxType.INBOX -> items
                            InboxType.ARCHIVED -> archivedItems
                            InboxType.ENCRYPTED -> encryptedItems
                            InboxType.BLOCKED -> blockedItems
                            InboxType.DRAFTS -> draftsItems
                            InboxType.MUTED -> mutedItems
                        },
                        key = { index, message -> message.thread_id!! }
                    ) { index, message ->
                        message.address?.let { address ->
                            val isBlocked by remember { mutableStateOf(BlockedNumberContract
                                .isBlocked(context, message.address)) }
                            val contactName: String? by remember { mutableStateOf(Contacts
                                .retrieveContactName(context, message.address))
                            }
                            var firstName = message.address
                            var lastName = ""
                            if (!contactName.isNullOrEmpty()) {
                                contactName!!.split(" ").let {
                                    firstName = it[0]
                                    if (it.size > 1)
                                        lastName = it[1]
                                }
                            }

                            var isMute by remember { mutableStateOf( false) }
                            LaunchedEffect(message.thread_id) {
                                coroutineScope.launch {
                                    isMute = conversationsViewModel.isMuted(context,
                                        message.thread_id)
                                }
                            }

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    when(it) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            slideDeleteItem.value = message.thread_id!!
                                            rememberDeleteMenu = true
                                            return@rememberSwipeToDismissBoxState false
                                        }
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            coroutineScope.launch {
                                                when(inboxType) {
                                                    InboxType.ARCHIVED ->
                                                        conversationsViewModel.unArchive(context,
                                                            message.thread_id)
                                                    else -> conversationsViewModel.archive(context,
                                                        message.thread_id)
                                                }
                                            }
                                            return@rememberSwipeToDismissBoxState true
                                        }
                                        SwipeToDismissBoxValue.Settled ->
                                            return@rememberSwipeToDismissBoxState false
                                        else -> {}
                                    }
                                    return@rememberSwipeToDismissBoxState true
                                },
                                positionalThreshold = { it * .75f }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    SwipeToDeleteBackground(
                                        dismissState,
                                        inboxType == InboxType.ARCHIVED
                                    )
                                }
                            ) {
                                ThreadConversationCard(
                                    id = message.thread_id!!,
                                    firstName = firstName!!,
                                    lastName = lastName,
                                    phoneNumber = address,
                                    content = if(message.text.isNullOrBlank())
                                        stringResource(R.string.conversation_threads_secured_content)
                                    else message.text!!,
                                    date =
                                    if(!message.date.isNullOrBlank())
                                        Helpers.formatDate(context, message.date!!.toLong())
                                    else "Tues",
                                    isRead = message.isRead,
                                    isContact = !contactName.isNullOrBlank(),
                                    isBlocked = isBlocked,
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if(selectedItems.isEmpty()) {
                                                navigateToConversation(
                                                    conversationsViewModel = conversationsViewModel,
                                                    address = message.address!!,
                                                    threadId = message.thread_id!!,
                                                    subscriptionId =
                                                    SIMHandler.getDefaultSimSubscription(context),
                                                    navController = navController,
                                                )
                                            } else {
                                                if(selectedItems.contains(message))
                                                    selectedItems.remove(message)
                                                else
                                                    selectedItems.add(message)
                                            }
                                        },
                                        onLongClick = {
                                            selectedItems.add(message)
                                        }
                                    ),
                                    isSelected = selectedItems.contains(message),
                                    isMuted = isMute,
                                    type = message.type
                                )
                            }
                        }
                    }
                }

                if(rememberDeleteMenu) {
                    DeleteConfirmationAlert(
                        confirmCallback = {
                            coroutineScope.launch {
                                val threads: List<String> = selectedItems.map { it.thread_id!! }
                                conversationsViewModel.deleteThreads(context,
                                    if(threads.isNotEmpty()) threads
                                    else listOf<String>(slideDeleteItem.value)
                                )
                                selectedItems.clear()
                                rememberDeleteMenu = false
                            }
                        }
                    ) {
                        rememberDeleteMenu = false
                        selectedItems.clear()
                    }
                }

                if(rememberImportMenuExpanded) {
                    val importConversations by remember { mutableStateOf(conversationsViewModel
                        .importAll(context, detailsOnly = true)) }
                    val numThreads by remember { mutableStateOf(
                        importConversations.map { it.thread_id }.toSet()
                    ) }
                    ImportDetails(
                        numOfConversations = importConversations.size,
                        numOfThreads = numThreads.size,
                        resetConfirmCallback = {
                            coroutineScope.launch {
                                conversationsViewModel.clear(context)
                                conversationsViewModel.importAll(context)
                                conversationsViewModel.importDetails = ""
                            }
                        },
                        confirmCallback = {
                            coroutineScope.launch {
                                conversationsViewModel.importAll(context)
                                conversationsViewModel.importDetails = ""
                            }
                    }) {
//                        rememberImportMenuExpanded = false
                        conversationsViewModel.importDetails = ""
                    }
                }

            }
        }

    }


}

@Preview
@Composable
fun PreviewMessageCard() {
    AppTheme(darkTheme = true) {
        Surface(Modifier.safeDrawingPadding()) {
            var messages: MutableList<Conversation> =
                remember { mutableListOf( ) }
            for(i in 0..10) {
                val thread = Conversation()
                thread.thread_id = i.toString()
                thread.address = "$i"
                thread.text = "Hello world: $i"
                thread.date = ""
                messages.add(thread)
            }
            ThreadConversationLayout(navController = rememberNavController(), _items = messages)
        }
    }


}

