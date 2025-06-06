package com.afkanerd.deku.DefaultSMS.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.BlockedNumberContract
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState.Loading
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.liveData
import com.afkanerd.deku.DefaultSMS.AdaptersViewModels.ConversationsViewModel
import com.afkanerd.deku.DefaultSMS.Commons.Helpers
import com.afkanerd.deku.ComposeNewMessageScreen
import com.afkanerd.deku.ConversationsScreen
import com.afkanerd.deku.DefaultSMS.Extensions.isScrollingUp
import com.afkanerd.deku.DefaultSMS.Models.Contacts
import com.afkanerd.deku.DefaultSMS.Models.Conversations.Conversation
import com.afkanerd.deku.DefaultSMS.Models.Conversations.ThreadedConversationsHandler
import com.afkanerd.deku.DefaultSMS.Models.SIMHandler
import com.afkanerd.deku.DefaultSMS.Models.SettingsHandler
import com.afkanerd.deku.DefaultSMS.R
import com.afkanerd.deku.SearchThreadScreen
import com.afkanerd.deku.DefaultSMS.ui.Components.DeleteConfirmationAlert
import com.afkanerd.deku.DefaultSMS.ui.Components.ImportDetails
import com.afkanerd.deku.DefaultSMS.ui.Components.ModalDrawerSheetLayout
import com.afkanerd.deku.DefaultSMS.ui.Components.SwipeToDeleteBackground
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadConversationCard
import com.afkanerd.deku.DefaultSMS.ui.Components.ThreadsMainDropDown
import com.afkanerd.deku.Modules.Subroutines
import com.afkanerd.deku.RemoteListenersAddScreen
import com.afkanerd.deku.RemoteListenersScreen
import com.example.compose.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class InboxType(val value: Int) {
    INBOX(0),
    ARCHIVED(1),
    ENCRYPTED(2),
    BLOCKED(3),
    DRAFTS(4),
    MUTED(5),
    REMOTE_LISTENER(6);

    companion object {
        fun fromInt(value: Int): InboxType? {
            return InboxType.entries.find { it.value == value }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun ThreadConversationLayout(
    conversationsViewModel: ConversationsViewModel,
    navController: NavController,
    _items: List<Conversation> = emptyList(),
) {
    val inPreviewMode = LocalInspectionMode.current
    val context = LocalContext.current

    var isDefault by remember{ mutableStateOf(inPreviewMode || Subroutines.isDefault(context)) }

    LaunchedEffect(true) {
        isDefault = Subroutines.isDefault(context)
    }

    val newIntent by conversationsViewModel.newIntent.collectAsState()
    LaunchedEffect(newIntent) {
        newIntent?.let {
            val defaultRegion = if(inPreviewMode) "cm" else Helpers.getUserCountry(context)
            conversationsViewModel.processIntents(context, it, defaultRegion)?.let {
                conversationsViewModel.setNewIntent(null)
                it.first?.let{ address ->
                    it.second?.let { threadId ->
                        it.third?.let{ message ->
                            conversationsViewModel.text = message
                        }
                        conversationsViewModel.navigateToConversation(
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
    }

    val counts by conversationsViewModel.getCount(context).observeAsState(null)

    var inboxType by remember { mutableStateOf(
        conversationsViewModel.getInboxType(isDefault)
    ) }

    val inboxMessagesPagers = conversationsViewModel
        .getThreadingPagingSource(context)

    val archivedMessagesPagers = conversationsViewModel
        .getArchivedPagingSource(context)

    val encryptedMessagesPagers = conversationsViewModel
        .getEncryptedPagingSource(context)

    val draftMessagesPagers = conversationsViewModel
        .getDraftPagingSource(context)

    val mutedMessagesPagers = conversationsViewModel
        .getMutedPagingSource(context)

    val remoteMessagesPagers = conversationsViewModel
        .getRemoteListenersPagingSource(context)

    val inboxMessagesItems = inboxMessagesPagers.collectAsLazyPagingItems()
    val archivedMessagesItems = archivedMessagesPagers.collectAsLazyPagingItems()
    val encryptedMessagesItems = encryptedMessagesPagers.collectAsLazyPagingItems()
    val draftMessagesItems = draftMessagesPagers.collectAsLazyPagingItems()
    val mutedMessagesItems = mutedMessagesPagers.collectAsLazyPagingItems()
    val remoteMessagesItems = remoteMessagesPagers.collectAsLazyPagingItems()

    var blockedItems: MutableList<Conversation> = remember { mutableStateListOf() }

    val listState = rememberLazyListState()
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var selectedItems = remember { mutableStateListOf<Conversation>() }
    var slideDeleteItem = remember { mutableStateOf("") }

    val selectedIconColors = MaterialTheme.colorScheme.primary
    var selectedItemIndex by remember { mutableStateOf(conversationsViewModel.inboxType) }

    var rememberImportMenuExpanded by remember { mutableStateOf( false)}
    var rememberDeleteMenu by remember { mutableStateOf( false)}

    val scope = rememberCoroutineScope()
    val coroutineScope = remember { CoroutineScope(Dispatchers.Default) }

    LaunchedEffect(inboxType) {
        selectedItemIndex = inboxType
        if(inboxType == InboxType.BLOCKED && isDefault) {
            coroutineScope.launch {
                conversationsViewModel.get(context).let {
                    blockedItems.addAll(it.filter {
                        BlockedNumberContract.isBlocked(context, it.address)
                    })
                }
            }
        }
    }

    LaunchedEffect(remoteMessagesItems) {
        if(!isDefault && remoteMessagesItems.itemCount > 0)
            inboxType = InboxType.REMOTE_LISTENER
    }

    LaunchedEffect(conversationsViewModel.importDetails) {
        rememberImportMenuExpanded = conversationsViewModel.importDetails.isNotBlank()
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

    var rememberMenuExpanded by remember { mutableStateOf( false)}
    ThreadsMainDropDown(
        expanded=rememberMenuExpanded,
        conversationViewModel = conversationsViewModel,
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
                if(selectedItems.isEmpty() && inboxType == InboxType.INBOX) {
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
                            if(isDefault || inPreviewMode) {
                                IconButton(onClick = {
                                    navController.navigate(SearchThreadScreen)
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = stringResource(R.string.search_messages)
                                    )
                                }
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
                else if(selectedItems.isNotEmpty()) {
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
                                    InboxType.REMOTE_LISTENER ->
                                        stringResource(R.string.remote_listeners)
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
                        actions = {
                            when(inboxType) {
                                InboxType.INBOX -> {}
                                InboxType.ARCHIVED -> {}
                                InboxType.ENCRYPTED -> {}
                                InboxType.BLOCKED -> {}
                                InboxType.DRAFTS -> {}
                                InboxType.MUTED -> {}
                                InboxType.REMOTE_LISTENER -> {
                                    IconButton(onClick = {
                                        navController.navigate(RemoteListenersAddScreen)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircle,
                                            contentDescription =
                                                stringResource(R.string.new_remote_listener)
                                        )
                                    }
                                }
                            }
                        },
                        scrollBehavior = scrollBehaviour
                    )
                }
            },
            floatingActionButton = {
                when(inboxType) {
                    InboxType.INBOX -> {
                        if(isDefault || inPreviewMode) {
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
                    }
                    InboxType.ARCHIVED -> {}
                    InboxType.ENCRYPTED -> {}
                    InboxType.BLOCKED -> {}
                    InboxType.DRAFTS -> {}
                    InboxType.MUTED -> {}
                    InboxType.REMOTE_LISTENER -> {
                        ExtendedFloatingActionButton(
                            onClick = {
                                navController.navigate(RemoteListenersScreen)
                            },
                            icon = { Icon( Icons.Default.Settings,
                                stringResource(R.string.settings)
                            ) },
                            text = { Text(text = "Configure") },
                            expanded = listState.isScrollingUp()
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                if(!isDefault && inboxType != InboxType.REMOTE_LISTENER) {
                    DefaultCheckMain {
                        conversationsViewModel.loadNatives(context, conversationsViewModel)
                        isDefault = true
                    }
                }
                else {
                    when(inboxType) {
                        InboxType.INBOX -> {
                            if(inboxMessagesItems.loadState.refresh != Loading &&
                                inboxMessagesItems.itemCount < 1)
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
                            if(archivedMessagesItems.loadState.refresh != Loading &&
                                archivedMessagesItems.itemCount < 1)
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
                        InboxType.REMOTE_LISTENER -> {
                            if(remoteMessagesItems.loadState.refresh != Loading &&
                                remoteMessagesItems.itemCount < 1)
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        stringResource(R.string
                                            .no_messages_sent_from_remote_listeners),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                        }
                    }
//
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    )  {
                        items(
                            count = when(inboxType) {
                                InboxType.BLOCKED -> blockedItems.size
                                InboxType.INBOX -> inboxMessagesItems.itemCount
                                InboxType.ARCHIVED -> archivedMessagesItems.itemCount
                                InboxType.ENCRYPTED -> encryptedMessagesItems.itemCount
                                InboxType.DRAFTS -> draftMessagesItems.itemCount
                                InboxType.MUTED -> mutedMessagesItems.itemCount
                                InboxType.REMOTE_LISTENER -> remoteMessagesItems.itemCount
                            },
                            key = when(inboxType) {
                                InboxType.BLOCKED -> {{ blockedItems[it].id }}
                                InboxType.INBOX -> inboxMessagesItems.itemKey{ it.id }
                                InboxType.ARCHIVED -> archivedMessagesItems.itemKey{ it.id }
                                InboxType.ENCRYPTED -> encryptedMessagesItems.itemKey{ it.id }
                                InboxType.DRAFTS -> draftMessagesItems.itemKey{ it.id }
                                InboxType.MUTED -> mutedMessagesItems.itemKey{ it.id }
                                InboxType.REMOTE_LISTENER -> remoteMessagesItems.itemKey{ it.id }
                            }
                        ) { index ->
                            val message = when(inboxType) {
                                InboxType.INBOX -> inboxMessagesItems[index]
                                InboxType.ARCHIVED -> archivedMessagesItems[index]
                                InboxType.ENCRYPTED -> encryptedMessagesItems[index]
                                InboxType.BLOCKED -> blockedItems[index]
                                InboxType.DRAFTS -> draftMessagesItems[index]
                                InboxType.MUTED -> mutedMessagesItems[index]
                                InboxType.REMOTE_LISTENER -> remoteMessagesItems[index]
                            }

                            message?.address?.let { address ->
                                val isBlocked by remember { mutableStateOf(
                                    if(isDefault)
                                        BlockedNumberContract.isBlocked(context, message.address)
                                    else false
                                )}

                                val contactName: String? by remember { mutableStateOf(
                                    if(isDefault)
                                        Contacts.retrieveContactName(context, message.address)
                                    else message.address
                                )}

                                var firstName = message.address
                                var lastName = ""
                                val isSelected = selectedItems.contains(message)
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
                                        }
                                        return@rememberSwipeToDismissBoxState true
                                    },
//                                    positionalThreshold = { it * .75f }
                                    positionalThreshold = { it * .85f }
                                )

                                val date by remember{ mutableStateOf(
                                        if(!message.date.isNullOrBlank())
                                            Helpers.formatDate(context, message.date!!.toLong())
                                        else "Tues",
                                ) }

                                val content by remember { mutableStateOf(
                                    if(message.text.isNullOrBlank())
                                        context.getString(R.string
                                            .conversation_threads_secured_content)
                                    else message.text!!,
                                ) }

                                SwipeToDismissBox(
                                    state = dismissState,
                                    gesturesEnabled = SettingsHandler.canSwipe(context),
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
                                        content = content,
                                        date = date,
                                        isRead = message.isRead,
                                        isContact = isDefault && !contactName.isNullOrBlank(),
                                        isBlocked = isBlocked,
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                if(selectedItems.isEmpty()) {
                                                    conversationsViewModel.navigateToConversation(
                                                        conversationsViewModel,
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
                                        isSelected = isSelected,
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
                            conversationsViewModel.importDetails = ""
                        }
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
            ThreadConversationLayout(
                navController = rememberNavController(),
                _items = messages,
                conversationsViewModel = ConversationsViewModel()
            )
        }
    }
}

@Preview
@Composable
fun PreviewMessageCardRemoteListeners_Preview() {
    AppTheme(darkTheme = true) {
        Surface(Modifier.safeDrawingPadding()) {
            ThreadConversationLayout(
                navController = rememberNavController(),
                _items = emptyList(),
                conversationsViewModel = ConversationsViewModel().apply {
                    inboxType = InboxType.REMOTE_LISTENER
                }
            )
        }
    }
}