package com.example.hava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.example.hava.ui.theme.HavaTheme
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface EkranDurumu {
    object Bos : EkranDurumu
    object Yukleniyor : EkranDurumu
    data class Basarili(val havaVerisi: HavaDurumuYaniti, val tamSehirAdi: String) : EkranDurumu
    data class Hata(val mesaj: String) : EkranDurumu
}

class HavaDurumuViewModel : ViewModel() {
    var durum by mutableStateOf<EkranDurumu>(EkranDurumu.Bos)
        private set

    private val apiAnahtari = "277eef9d13911c6a75060a29f547c9d1"
    private var sonArananSehir = ""
    private var sonArananTamAd = ""

    val favoriler = mutableStateListOf("58 Sivas", "81 Düzce", "16 Bursa")

    fun favoriDurumunuDegistir(sehirTamAd: String) {
        if (favoriler.contains(sehirTamAd)) {
            favoriler.remove(sehirTamAd)
        } else {
            favoriler.add(sehirTamAd)
        }
    }

    fun sehirAra(sadeceAd: String, tamAd: String = sadeceAd) {
        if (sadeceAd.isBlank()) {
            durum = EkranDurumu.Bos
            return
        }
        sonArananSehir = sadeceAd
        sonArananTamAd = tamAd
        durum = EkranDurumu.Yukleniyor

        viewModelScope.launch {
            try {
                val yanit = AgIstemcisi.api.havaDurumunuGetir(sadeceAd, apiAnahtari)
                durum = EkranDurumu.Basarili(yanit, tamAd)
            } catch (e: Exception) {
                durum = EkranDurumu.Hata("Bağlantı hatası veya şehir bulunamadı.")
            }
        }
    }

    fun tekrarDene() {
        if (sonArananSehir.isNotEmpty()) sehirAra(sonArananSehir, sonArananTamAd)
    }
}

fun selamlamayiGetir(): String {
    val saat = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (saat) {
        in 6..11 -> "Günaydın ☀️"
        in 12..17 -> "İyi Günler 🌤️"
        in 18..22 -> "İyi Akşamlar 🌙"
        else -> "İyi Geceler ✨"
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HavaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Hava Durumu", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    HavaDurumuEkrani(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HavaDurumuEkrani(
    modifier: Modifier = Modifier,
    viewModel: HavaDurumuViewModel = viewModel()
) {
    var aramaMetni by rememberSaveable { mutableStateOf("") }
    val klavyeKontrol = LocalSoftwareKeyboardController.current
    val anlikSelamlama = remember { selamlamayiGetir() }

    val tumSehirler = listOf(
        "01 Adana", "02 Adıyaman", "03 Afyonkarahisar", "04 Ağrı", "05 Amasya", "06 Ankara", "07 Antalya", "08 Artvin", "09 Aydın", "10 Balıkesir",
        "11 Bilecik", "12 Bingöl", "13 Bitlis", "14 Bolu", "15 Burdur", "16 Bursa", "17 Çanakkale", "18 Çankırı", "19 Çorum", "20 Denizli",
        "21 Diyarbakır", "22 Edirne", "23 Elazığ", "24 Erzincan", "25 Erzurum", "26 Eskişehir", "27 Gaziantep", "28 Giresun", "29 Gümüşhane", "30 Hakkari",
        "31 Hatay", "32 Isparta", "33 Mersin", "34 İstanbul", "35 İzmir", "36 Kars", "37 Kastamonu", "38 Kayseri", "39 Kırklareli", "40 Kırşehir",
        "41 Kocaeli", "42 Konya", "43 Kütahya", "44 Malatya", "45 Manisa", "46 Kahramanmaraş", "47 Mardin", "48 Muğla", "49 Muş", "50 Nevşehir",
        "51 Niğde", "52 Ordu", "53 Rize", "54 Sakarya", "55 Samsun", "56 Siirt", "57 Sinop", "58 Sivas", "59 Tekirdağ", "60 Tokat",
        "61 Trabzon", "62 Tunceli", "63 Şanlıurfa", "64 Uşak", "65 Van", "66 Yozgat", "67 Zonguldak", "68 Aksaray", "69 Bayburt", "70 Karaman",
        "71 Kırıkkale", "72 Batman", "73 Şırnak", "74 Bartın", "75 Ardahan", "76 Iğdır", "77 Yalova", "78 Karabük", "79 Kilis", "80 Osmaniye", "81 Düzce"
    )

    val filtrelenmisSehirler = tumSehirler.filter { it.contains(aramaMetni, ignoreCase = true) }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {

        if (viewModel.durum is EkranDurumu.Bos && aramaMetni.isEmpty()) {
            Text(
                text = "$anlikSelamlama\nBugün nereyi merak ediyorsun?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
            )
        }

        OutlinedTextField(
            value = aramaMetni,
            onValueChange = {
                aramaMetni = it
                if (it.isBlank()) viewModel.sehirAra("")
            },
            label = { Text("Şehir Ara (Örn: Sivas veya 58)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (aramaMetni.isNotBlank()) {
                        val aramaSorgusu = aramaMetni.substringAfter(" ")
                        val tamAd = tumSehirler.find { it.contains(aramaSorgusu, true) } ?: aramaMetni
                        viewModel.sehirAra(aramaSorgusu, tamAd)
                    }
                    klavyeKontrol?.hide()
                }
            ),
            trailingIcon = {
                if (aramaMetni.isNotEmpty()) {
                    IconButton(onClick = {
                        aramaMetni = ""
                        viewModel.sehirAra("")
                        klavyeKontrol?.hide()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Ara", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (val anlikDurum = viewModel.durum) {
            is EkranDurumu.Bos -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {

                    if (aramaMetni.isEmpty() && viewModel.favoriler.isNotEmpty()) {
                        item {
                            Text("📌 Hızlı Erişim", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(viewModel.favoriler) { sehir ->
                                    FavoriKarti(sehirTamAd = sehir, onClick = {
                                        val sadeceAd = sehir.substringAfter(" ")
                                        aramaMetni = sehir
                                        viewModel.sehirAra(sadeceAd, sehir)
                                        klavyeKontrol?.hide()
                                    })
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("🌍 Tüm Şehirler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    itemsIndexed(filtrelenmisSehirler) { index, sehirTamAd ->
                        SehirListeElemani(
                            sehirTamAd = sehirTamAd,
                            index = index,
                            isFavori = viewModel.favoriler.contains(sehirTamAd),
                            onFavoriClick = { viewModel.favoriDurumunuDegistir(sehirTamAd) },
                            onClick = {
                                val sadeceAd = sehirTamAd.substringAfter(" ")
                                aramaMetni = sehirTamAd
                                viewModel.sehirAra(sadeceAd, sehirTamAd)
                                klavyeKontrol?.hide()
                            }
                        )
                    }
                }
            }
            is EkranDurumu.Yukleniyor -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }
            }
            is EkranDurumu.Basarili -> {
                val veri = anlikDurum.havaVerisi
                val tamAd = anlikDurum.tamSehirAdi
                val sicaklikDegeri = veri.anaVeri.sicaklik
                val aciklama = veri.havaDurumuBilgisi.firstOrNull()?.aciklama?.lowercase() ?: ""

                val giyimOnerisi = when {
                    sicaklikDegeri < 5.0 -> "Çok soğuk! 🥶 Kalın bir mont, bere ve eldiven şart."
                    sicaklikDegeri < 15.0 -> "Hava serin. 🧥 Üzerine bir ceket veya kazak al."
                    sicaklikDegeri < 25.0 -> "Hava çok güzel! 👕 Tişört ve ince bir hırka yeterli."
                    else -> "Oldukça sıcak! 🕶️ Şort ve güneş gözlüğü şahane olur."
                }

                val aktiviteOnerisi = when {
                    aciklama.contains("yağmur") || aciklama.contains("kar") -> "Dışarısı yağışlı. ☕ Evde kahve eşliğinde kodlama yap!"
                    sicaklikDegeri < 15.0 -> "Kapalı mekanları değerlendir. 🏛️ Sinema veya kafe harika olur."
                    else -> "Hemen dışarı çık! 🚲 Yürüyüş veya bisiklet için mükemmel fırsat."
                }

                SonucKarti(
                    sehirAdi = veri.sehirAdi,
                    sicaklikDegeri = sicaklikDegeri,
                    aciklama = veri.havaDurumuBilgisi.firstOrNull()?.aciklama?.replaceFirstChar { it.uppercase() } ?: "",
                    giyimOneri = giyimOnerisi,
                    aktiviteOneri = aktiviteOnerisi,
                    isFavori = viewModel.favoriler.contains(tamAd),
                    onFavoriClick = { viewModel.favoriDurumunuDegistir(tamAd) }
                )
            }
            is EkranDurumu.Hata -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(anlikDurum.mesaj, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 32.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.tekrarDene() }, shape = RoundedCornerShape(16.dp)) {
                        Text("Tekrar Dene")
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriKarti(sehirTamAd: String, onClick: () -> Unit) {
    var sicaklik by rememberSaveable { mutableStateOf("...") }
    val sadeceAd = sehirTamAd.substringAfter(" ")
    val apiAnahtari = "277eef9d13911c6a75060a29f547c9d1"

    LaunchedEffect(sadeceAd) {
        if (sicaklik == "...") {
            try {
                val yanit = AgIstemcisi.api.havaDurumunuGetir(sadeceAd, apiAnahtari)
                sicaklik = "${yanit.anaVeri.sicaklik.toInt()}°C"
            } catch (e: Exception) {
                sicaklik = "-"
            }
        }
    }

    Card(
        modifier = Modifier.width(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⭐", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = sadeceAd, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = sicaklik, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SehirListeElemani(sehirTamAd: String, index: Int, isFavori: Boolean, onFavoriClick: () -> Unit, onClick: () -> Unit) {
    var sicaklik by rememberSaveable { mutableStateOf("...") }
    val apiAnahtari = "277eef9d13911c6a75060a29f547c9d1"
    val sadeceAd = sehirTamAd.substringAfter(" ")

    val pastelRenkler = listOf(
        Color(0xFFFFE3E3), Color(0xFFFFF5D1), Color(0xFFE3F2FD),
        Color(0xFFE8F5E9), Color(0xFFF3E5F5), Color(0xFFFFF3E0)
    )
    val kartRengi = pastelRenkler[index % pastelRenkler.size]

    LaunchedEffect(sadeceAd) {
        if (sicaklik == "...") {
            try {
                val yanit = AgIstemcisi.api.havaDurumunuGetir(sadeceAd, apiAnahtari)
                sicaklik = "${yanit.anaVeri.sicaklik.toInt()}°C"
            } catch (e: Exception) {
                sicaklik = "-"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = kartRengi)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriClick) {
                    Icon(
                        imageVector = if (isFavori) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favori",
                        tint = if (isFavori) Color(0xFFFFB300) else Color(0xFF757575)
                    )
                }
                Text(sehirTamAd, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFF424242))
            }
            Text(sicaklik, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFF424242))
        }
    }
}

@Composable
fun DetayKutusu(ikon: String, baslik: String, deger: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = ikon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = baslik, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        Text(text = deger, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
fun SonucKarti(sehirAdi: String, sicaklikDegeri: Double, aciklama: String, giyimOneri: String, aktiviteOneri: String, isFavori: Boolean, onFavoriClick: () -> Unit) {
    val arkaPlanRengi = when {
        sicaklikDegeri < 15.0 -> Brush.linearGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1)))
        sicaklikDegeri < 25.0 -> Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFF57C00)))
        else -> Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFE64A19)))
    }

    val lottieUrl = when {
        aciklama.lowercase().contains("yağmur") -> "https://assets9.lottiefiles.com/temp/lf20_Jj2Qzw.json"
        aciklama.lowercase().contains("bulut") -> "https://assets9.lottiefiles.com/temp/lf20_dgjK9i.json"
        else -> "https://assets9.lottiefiles.com/temp/lf20_Stdaec.json"
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Url(lottieUrl))

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(arkaPlanRengi).padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(sehirAdi, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            if (aciklama.isNotEmpty()) {
                                Text(aciklama, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onFavoriClick) {
                                Icon(
                                    imageVector = if (isFavori) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Favori",
                                    tint = if (isFavori) Color(0xFFFFD54F) else Color.White
                                )
                            }
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${sicaklikDegeri.toInt()}°C", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light), color = Color.White)

                    Spacer(modifier = Modifier.height(16.dp))

                    // YENİ EKLENEN HAVA DURUMU DETAY SATIRI (Nem, Hissedilen, Rüzgar)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetayKutusu(ikon = "💧", baslik = "Nem", deger = "%45")
                        DetayKutusu(ikon = "🌡️", baslik = "Hissedilen", deger = "${(sicaklikDegeri + 2).toInt()}°C")
                        DetayKutusu(ikon = "🌬️", baslik = "Rüzgar", deger = "12 km/s")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("⏳ Saatlik Tahmin", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    val saatler = listOf("Şimdi", "1 Sa", "2 Sa", "3 Sa", "4 Sa", "5 Sa")
                    val tahminler = listOf(sicaklikDegeri.toInt(), (sicaklikDegeri+1).toInt(), (sicaklikDegeri+2).toInt(), (sicaklikDegeri+1).toInt(), (sicaklikDegeri-1).toInt(), (sicaklikDegeri-2).toInt())

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(saatler.size) { i ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(saatler[i], style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${tahminler[i]}°", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 Ne Giysem?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(giyimOneri, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🎯 Ne Yapsam?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(aktiviteOneri, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}