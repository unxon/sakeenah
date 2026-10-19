package com.sakinah.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.reader.data.PassageEntity
import com.sakinah.reader.data.AnnotationEntity

private val Ink = Color(0xFF213936)
private val Deep = Color(0xFF153A36)
private val Sage = Color(0xFF6D8B7D)
private val Gold = Color(0xFFC99C52)
private val Paper = Color(0xFFF7F5EF)
private val Mist = Color(0xFFE7EEE8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { val settings: SettingsViewModel = viewModel(); val darkMode by settings.darkMode.collectAsState(); SakinahTheme(darkMode) { SakinahApp(settings) } } }
}

@Composable private fun SakinahTheme(darkMode: Boolean, content: @Composable () -> Unit) = MaterialTheme(colorScheme = if (darkMode) darkColorScheme(primary=Color(0xFF9CCAB5), secondary=Color(0xFFC9A45E), background=Color(0xFF121816), surface=Color(0xFF1B2421)) else lightColorScheme(primary=Deep, secondary=Sage, background=Paper, surface=Color.White), content=content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SakinahApp(settings: SettingsViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var panel by rememberSaveable { mutableStateOf<String?>(null) }
    val readerVm: ReaderViewModel = viewModel()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(bottomBar={ NavigationBar(containerColor=Color.White) {
            listOf("الرئيسية" to Icons.Default.Home, "القراءة" to Icons.Default.MenuBook, "البحث" to Icons.Default.Search, "المزيد" to Icons.Default.MoreHoriz).forEachIndexed { i, item ->
                NavigationBarItem(selected=tab==i && panel == null, onClick={if (i == 3) panel = "menu" else { panel = null; tab = i }}, icon={Icon(item.second, null)}, label={Text(item.first)})
            }
        }}) { padding -> Box(Modifier.padding(padding).fillMaxSize()) { when(tab) { 0 -> Home(openReader = { tab=1 }); 1 -> Reader(readerVm); else -> Search(onOpenPassage={ readerVm.openPassage(it); tab=1 }) } } }
        if (panel != null) ModalBottomSheet(onDismissRequest={ panel = null }) {
            when (panel) {
                "menu" -> MoreMenu(onLibrary={panel="library"}, onSaved={panel="saved"}, onSettings={panel="settings"})
                "library" -> Library(openReader={panel=null; tab=1})
                "saved" -> Saved(onOpen={ note -> readerVm.openNote(note) { readerVm.openPassage(it); panel = null; tab = 1 } })
                "settings" -> Settings(settings)
            }
        }
    }
}

@Composable private fun MoreMenu(onLibrary: () -> Unit, onSaved: () -> Unit, onSettings: () -> Unit) {
    Column(Modifier.padding(horizontal=22.dp, vertical=12.dp)) {
        Text("المزيد", fontSize=26.sp, fontWeight=FontWeight.Bold, color=Ink)
        Text("أدواتك ومساحتك الشخصية في مكان واحد", color=Sage, modifier=Modifier.padding(bottom=14.dp))
        ListItem(headlineContent={Text("المكتبة")}, supportingContent={Text("المصادر والسور المثبتة")}, leadingContent={Icon(Icons.Default.LibraryBooks, null, tint=Sage)}, modifier=Modifier.clickable(onClick=onLibrary))
        ListItem(headlineContent={Text("المحفوظات والملاحظات")}, supportingContent={Text("ما اخترت العودة إليه")}, leadingContent={Icon(Icons.Default.Bookmark, null, tint=Sage)}, modifier=Modifier.clickable(onClick=onSaved))
        ListItem(headlineContent={Text("الإعدادات")}, supportingContent={Text("المظهر والخصوصية والمحتوى")}, leadingContent={Icon(Icons.Default.Settings, null, tint=Sage)}, modifier=Modifier.clickable(onClick=onSettings))
        Spacer(Modifier.height(18.dp))
    }
}

@Composable private fun Library(openReader: () -> Unit, catalog: CatalogViewModel = viewModel()) {
    val works by catalog.works.collectAsState()
    LazyColumn(contentPadding=PaddingValues(22.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Text("المكتبة", fontSize=30.sp, fontWeight=FontWeight.Bold, color=Ink); Text("اختر رفيق القراءة من مصادرك المثبتة", color=Sage) }
        item { OutlinedTextField("", {}, Modifier.fillMaxWidth(), enabled=false, placeholder={Text("بحث في المكتبة")}, leadingIcon={Icon(Icons.Default.Search, null)}) }
        item { Text("المصادر المثبتة", fontWeight=FontWeight.Bold, color=Ink, modifier=Modifier.padding(top=8.dp)) }
        if (works.isEmpty()) item { Empty(Icons.Default.LibraryBooks, "جارٍ تجهيز المكتبة", "يتم تحميل catalog والمحتوى المحلي بأمان.") }
        else items(works, key={it.id}) { work ->
            Card(onClick=openReader, colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(22.dp), modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).background(Deep, RoundedCornerShape(16.dp)), contentAlignment=Alignment.Center) { Icon(Icons.Default.AutoStories, null, tint=Gold) }
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(work.title, fontSize=18.sp, fontWeight=FontWeight.Bold, color=Ink); Text(work.subtitle, color=Sage, fontSize=12.sp); Text("${work.verseCount} آية · مثبت على الجهاز", color=Sage, fontSize=12.sp) }
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint=Sage)
                }
            }
        }
        item { Card(colors=CardDefaults.cardColors(containerColor=Mist), shape=RoundedCornerShape(20.dp)) { Row(Modifier.padding(18.dp), verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Default.AddCircleOutline, null, tint=Sage); Spacer(Modifier.width(12.dp)); Column { Text("إضافة مصدر", fontWeight=FontWeight.Bold, color=Ink); Text("استيراد JSON موثوق من حزمة محتوى", color=Sage, fontSize=12.sp) } } } }
    }
}

@Composable private fun Brand() { Row(verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(46.dp).background(Deep, RoundedCornerShape(15.dp)), contentAlignment=Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint=Gold) }; Spacer(Modifier.width(12.dp)); Column { Text("سَكينة", fontSize=22.sp, fontWeight=FontWeight.Bold, color=Ink); Text("رفقة هادئة مع النص", fontSize=12.sp, color=Sage) } } }

@Composable private fun Home(openReader: () -> Unit, catalog: CatalogViewModel = viewModel()) {
    val works by catalog.works.collectAsState()
    LazyColumn(contentPadding=PaddingValues(22.dp), verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item { Brand() }
        item { Text("قراءة هادئة، من حيث توقفت", fontSize=28.sp, fontWeight=FontWeight.Bold, color=Ink); Text("اختر مصدرًا من المكتبة أو تابع آخر موضع محفوظ.", color=Sage) }
        item { Card(onClick=openReader, colors=CardDefaults.cardColors(containerColor=Deep), shape=RoundedCornerShape(24.dp), modifier=Modifier.fillMaxWidth()) { Row(Modifier.padding(22.dp), verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Default.MenuBook, null, tint=Gold, modifier=Modifier.size(32.dp)); Spacer(Modifier.width(16.dp)); Column(Modifier.weight(1f)) { Text("فتح القارئ", color=Color.White, fontSize=20.sp, fontWeight=FontWeight.Bold); Text("يبدأ من أول موضع متاح دون انتظار كامل المحتوى", color=Color(0xFFC9DBD0), fontSize=12.sp) }; Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint=Gold) } } }
        item { Text("مصادر مثبتة", fontSize=19.sp, fontWeight=FontWeight.Bold, color=Ink) }
        if (works.isEmpty()) item { Empty(Icons.Default.Sync, "جارٍ تجهيز المحتوى", "سيظهر أول نص فور توفر أول حزمة.") }
        else items(works.take(3), key={it.id}) { work -> LibraryCard(work.title, "${work.verseCount} آية · ${work.subtitle}", Icons.Default.AutoStories, Deep, openReader, Modifier.fillMaxWidth().height(112.dp)) }
    }
}

@Composable private fun LibraryCard(title:String, sub:String, icon:androidx.compose.ui.graphics.vector.ImageVector, color:Color, onClick:()->Unit, modifier:Modifier) { Card(onClick=onClick, modifier=modifier.height(140.dp), shape=RoundedCornerShape(20.dp), colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(16.dp)) { Icon(icon,null,tint=color,modifier=Modifier.size(26.dp)); Spacer(Modifier.weight(1f)); Text(title,fontWeight=FontWeight.Bold,color=Ink); Text(sub,fontSize=12.sp,color=Sage) } } }
@Composable private fun Stat(value:String,label:String,modifier:Modifier) { Card(modifier=modifier, shape=RoundedCornerShape(18.dp), colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.padding(vertical=14.dp).fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally) { Text(value,fontSize=20.sp,fontWeight=FontWeight.Bold,color=Ink); Text(label,fontSize=11.sp,color=Sage) } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun Reader(vm: ReaderViewModel = viewModel()) {
    var showTools by rememberSaveable { mutableStateOf(false) }
    var showChapters by rememberSaveable { mutableStateOf(false) }
    var noteTarget by rememberSaveable { mutableIntStateOf(0) }
    var noteText by rememberSaveable { mutableStateOf("") }
    val passages by vm.passages.collectAsState()
    val size by vm.fontSize.collectAsState()
    val saved by vm.saved.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val pendingVerse by vm.pendingVerse.collectAsState()
    val listState = rememberLazyListState()
    LaunchedEffect(pendingVerse, passages) { if (pendingVerse > 0 && passages.isNotEmpty()) { listState.scrollToItem((pendingVerse - 1).coerceAtMost(passages.size - 1)); vm.clearPendingVerse() } }
    Column {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
            IconButton({}) { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
            Column(Modifier.weight(1f).clickable { showChapters = true }, horizontalAlignment=Alignment.CenterHorizontally) {
                Text(passages.firstOrNull()?.chapterTitle ?: "جاري تجهيز النص", fontWeight=FontWeight.Bold, fontSize=18.sp, color=Ink)
                Text(if (passages.isEmpty()) "يتم تحميل أول مقطع" else "الآيات المحملة · ${passages.size}", fontSize=12.sp, color=Sage)
            }
            IconButton({ showTools = true }) { Icon(Icons.Default.MoreHoriz, null) }
        }
        Row(Modifier.padding(horizontal=20.dp).fillMaxWidth().background(Mist, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint=Sage); Spacer(Modifier.width(8.dp)); Text("قراءة محلية — لا تحتاج إلى اتصال", fontSize=12.sp, color=Ink)
        }
        if (passages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment=Alignment.Center) { CircularProgressIndicator(color=Sage) }
        } else {
            LazyColumn(state=listState, contentPadding=PaddingValues(24.dp), modifier=Modifier.weight(1f)) {
                item { Text(passages.first().originalText, fontSize=(size - 3).sp, color=Sage, textAlign=TextAlign.Center, modifier=Modifier.fillMaxWidth().padding(bottom=18.dp)) }
                items(passages.drop(1), key={ it.number }) { passage ->
                    val active = saved.contains(passage.number)
                    Row(Modifier.fillMaxWidth().padding(vertical=4.dp).background(if(active) Color(0xFFEFE8D8) else Color.Transparent, RoundedCornerShape(16.dp)).padding(10.dp), verticalAlignment=Alignment.Top) {
                        Text("${passage.number}", color=Sage, modifier=Modifier.padding(6.dp))
                        Text(passage.originalText, fontSize=size.sp, lineHeight=(size * 1.8f).sp, color=Ink, modifier=Modifier.weight(1f))
                        IconButton({ vm.toggleBookmark(passage.number) }) { Icon(if(active) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null, tint=Gold) }
                        IconButton({ noteTarget = passage.number; noteText = ""; showTools = true }) { Icon(Icons.Default.EditNote, null, tint=Sage) }
                    }
                }
            }
        }
    }
    if (showChapters) ModalBottomSheet(onDismissRequest={ showChapters = false }) {
        Column(Modifier.padding(horizontal=22.dp, vertical=12.dp)) {
            Text("اختر السورة", fontSize=24.sp, fontWeight=FontWeight.Bold, color=Ink)
            Text("سيظهر النص فور اختيارها من المحتوى المحلي.", color=Sage, modifier=Modifier.padding(bottom=10.dp))
            LazyColumn { items(chapters, key={it.first}) { chapter -> ListItem(headlineContent={Text("${chapter.first}. ${chapter.second}")}, supportingContent={Text("فتح السورة")}, leadingContent={Icon(Icons.Default.AutoStories, null, tint=Sage)}, modifier=Modifier.clickable { vm.selectChapter(chapter.first); showChapters = false }) } }
        }
    }
    if (showTools) ModalBottomSheet(onDismissRequest={ showTools = false }) {
        Column(Modifier.padding(24.dp)) {
            Text(if (noteTarget > 0) "ملاحظة على الآية $noteTarget" else "أدوات القراءة", fontSize=22.sp, fontWeight=FontWeight.Bold, color=Ink)
            Text(if (noteTarget > 0) "احفظ فكرة قصيرة مرتبطة بهذا الموضع." else "تظهر عند الطلب حتى تبقى مساحة النص هادئة.", color=Sage)
            Spacer(Modifier.height(18.dp))
            if (noteTarget > 0) {
                OutlinedTextField(noteText, { noteText = it }, Modifier.fillMaxWidth(), minLines=3, placeholder={Text("اكتب ملاحظتك…")})
                Spacer(Modifier.height(12.dp))
                Button(onClick={ vm.saveNote(noteTarget, noteText); noteTarget = 0; noteText = ""; showTools = false }, modifier=Modifier.fillMaxWidth()) { Text("حفظ الملاحظة") }
            } else {
                Text("حجم النص", fontWeight=FontWeight.SemiBold, color=Ink)
                Row(verticalAlignment=Alignment.CenterVertically) { Text("أ", fontSize=13.sp); Slider(size, { vm.setFontSize(it) }, valueRange=20f..34f, modifier=Modifier.weight(1f), colors=SliderDefaults.colors(thumbColor=Sage, activeTrackColor=Sage)); Text("أ", fontSize=20.sp) }
            }
        }
    }
}

@Composable private fun Search(onOpenPassage: (PassageEntity) -> Unit, vm: SearchViewModel = viewModel()) {
    var query by rememberSaveable { mutableStateOf("") }
    val results by vm.results.collectAsState()
    Column(Modifier.padding(22.dp)) {
        Text("البحث", fontSize=29.sp, fontWeight=FontWeight.Bold, color=Ink)
        Text("بحث عربي محلي داخل المحتوى المثبت", color=Sage)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(query, { query=it; vm.updateQuery(it) }, Modifier.fillMaxWidth(), placeholder={Text("اكتب كلمة أو عبارة…")}, leadingIcon={Icon(Icons.Default.Search,null)})
        Spacer(Modifier.height(22.dp))
        when {
            query.isBlank() -> Empty(Icons.Default.Search, "ابدأ بالبحث", "ستظهر النتائج من قاعدة البيانات المحلية مع اسم المصدر.")
            results.isEmpty() -> Empty(Icons.Default.SearchOff, "لا توجد نتائج", "جرّب كلمة أخرى أو أزل التشكيل من عبارة البحث.")
            else -> LazyColumn { items(results, key={ "${it.workId}:${it.number}" }) { result ->
                Column(Modifier.fillMaxWidth().clickable { onOpenPassage(result) }.padding(top=12.dp, bottom=12.dp)) {
                    Text(result.originalText, fontSize=18.sp, color=Ink)
                    Text("${result.chapterTitle} · آية ${result.verseNumber} · فتح في القارئ", fontSize=12.sp, color=Sage)
                }
                HorizontalDivider(color=Color(0xFFE4E9E4))
            } }
        }
    }
}

@Composable private fun Saved(onOpen: (AnnotationEntity) -> Unit, vm: ReaderViewModel = viewModel()) {
    val saved by vm.savedPassages.collectAsState()
    val notes by vm.notes.collectAsState()
    var editing by remember { mutableStateOf<AnnotationEntity?>(null) }
    var editText by rememberSaveable { mutableStateOf("") }
    LazyColumn(contentPadding=PaddingValues(22.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("المحفوظات والملاحظات",fontSize=29.sp,fontWeight=FontWeight.Bold,color=Ink); Text("كل ما اخترت الاحتفاظ به أثناء القراءة",color=Sage) }
        item { Text("مواضع محفوظة", fontWeight=FontWeight.Bold, color=Ink, modifier=Modifier.padding(top=8.dp)) }
        if (saved.isEmpty()) item { Empty(Icons.Default.BookmarkBorder,"لا توجد محفوظات بعد","احفظ آية من القارئ لتظهر هنا.") }
        else items(saved, key={ "${it.workId}:${it.number}" }) { passage ->
            Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(18.dp), modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically) {
                    Text("${passage.number}", color=Sage, modifier=Modifier.padding(end=12.dp))
                    Column(Modifier.weight(1f)) { Text(passage.originalText, fontSize=17.sp, color=Ink); Text("${passage.chapterTitle} · آية ${passage.verseNumber}", fontSize=11.sp, color=Sage) }
                    IconButton({ vm.toggleBookmark(passage.number) }) { Icon(Icons.Default.Bookmark, null, tint=Gold) }
                }
            }
        }
        item { Text("ملاحظاتك", fontWeight=FontWeight.Bold, color=Ink, modifier=Modifier.padding(top=10.dp)) }
        if (notes.isEmpty()) item { Empty(Icons.Default.EditNote,"لا توجد ملاحظات بعد","استخدم أيقونة القلم بجانب الآية لإنشاء ملاحظة.") }
        else items(notes, key={it.id}) { note ->
            Card(onClick={onOpen(note)}, colors=CardDefaults.cardColors(containerColor=Mist), shape=RoundedCornerShape(18.dp), modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment=Alignment.Top) {
                    Column(Modifier.weight(1f)) { Text("الآية ${note.passageNumber} · فتح في القارئ", fontSize=12.sp, fontWeight=FontWeight.Bold, color=Sage); Spacer(Modifier.height(5.dp)); Text(note.body, color=Ink, fontSize=16.sp, maxLines=4) }
                    IconButton({ editText = note.body; editing = note }) { Icon(Icons.Default.Edit, "تعديل", tint=Sage) }
                    IconButton({ vm.deleteNote(note) }) { Icon(Icons.Default.DeleteOutline, "حذف", tint=Color(0xFFB45F5F)) }
                }
            }
        }
    }
    editing?.let { note ->
        AlertDialog(onDismissRequest={editing=null}, title={Text("تعديل الملاحظة")}, text={OutlinedTextField(editText,{editText=it}, minLines=3, placeholder={Text("اكتب ملاحظتك…")})}, confirmButton={TextButton(onClick={vm.updateNote(note, editText); editing=null}) {Text("حفظ")}}, dismissButton={TextButton(onClick={editing=null}) {Text("إلغاء")}})
    }
}
@Composable private fun Settings(vm: SettingsViewModel) {
    val darkMode by vm.darkMode.collectAsState()
    LazyColumn(contentPadding=PaddingValues(22.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Text("الإعدادات",fontSize=30.sp,fontWeight=FontWeight.Bold,color=Ink); Text("خصّص سكينة بهدوء، دون تعقيد",color=Sage) }
        item { Text("المظهر والقراءة",fontWeight=FontWeight.Bold,color=Ink) }
        item { Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) { Column {
            ListItem(headlineContent={Text("الوضع الداكن")}, supportingContent={Text(if(darkMode) "مفعل — ألوان مريحة للعين" else "غير مفعل — مظهر نهاري هادئ")}, leadingContent={Icon(Icons.Default.DarkMode,null)}, trailingContent={Switch(darkMode,{vm.setDarkMode(it)})})
            HorizontalDivider(color=Color(0xFFE4E9E4))
            ListItem(headlineContent={Text("تخصيص القراءة")}, supportingContent={Text("حجم الخط من أدوات القراءة داخل القارئ")}, leadingContent={Icon(Icons.Default.TextFields,null)})
        } } }
        item { Text("المحتوى",fontWeight=FontWeight.Bold,color=Ink) }
        item { Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) { Column {
            ListItem(headlineContent={Text("المكتبة والمصادر")}, supportingContent={Text("إدارة الحزم من catalog والتحقق من المصدر")}, leadingContent={Icon(Icons.Default.LibraryBooks,null,tint=Sage)})
            HorizontalDivider(color=Color(0xFFE4E9E4))
            ListItem(headlineContent={Text("التنزيلات")}, supportingContent={Text("ستظهر هنا حزم المحتوى قيد التنزيل")}, leadingContent={Icon(Icons.Default.Download,null,tint=Sage)})
        } } }
        item { Text("الخصوصية والبيانات",fontWeight=FontWeight.Bold,color=Ink) }
        item { Card(colors=CardDefaults.cardColors(containerColor=Color.White), shape=RoundedCornerShape(20.dp)) { ListItem(headlineContent={Text("بياناتك على جهازك")}, supportingContent={Text("النسخ الاحتياطي المشفر والاستعادة ستضاف هنا")}, leadingContent={Icon(Icons.Default.Lock,null,tint=Sage)}) } }
        item { Text("سَكينة · إصدار داخلي 0.3.2",fontSize=12.sp,color=Sage,modifier=Modifier.padding(top=6.dp)) }
    }
}
@Composable private fun Empty(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,message:String){ Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(28.dp).fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=Sage,modifier=Modifier.size(42.dp));Spacer(Modifier.height(12.dp));Text(title,fontWeight=FontWeight.Bold,fontSize=17.sp,color=Ink);Spacer(Modifier.height(6.dp));Text(message,textAlign=TextAlign.Center,color=Sage)}} }
