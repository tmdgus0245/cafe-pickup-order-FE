package com.cafepickuporder.android.ui.mypage

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.CustomerPhoneUpdateRequest
import com.cafepickuporder.android.data.request.CustomerProfileUpdateRequest
import com.cafepickuporder.android.local.TokenManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange
import kotlinx.coroutines.launch

private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val phonePattern = Regex("^01[016789]-?\\d{3,4}-?\\d{4}$")

private data class EditableProfile(
    val name: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String?
)

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var customerName by remember {
        mutableStateOf(tokenManager.getCustomerName().orEmpty().ifBlank { "고객" })
    }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val token = tokenManager.getAccessToken() ?: return@LaunchedEffect

        try {
            val response = ApiClient.authApi.getMyInfo("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                customerName = profile.name.ifBlank { "고객" }
                customerEmail = profile.email
                customerPhone = profile.phone.orEmpty()
                profileImageUrl = profile.profileImageUrl
                tokenManager.saveCustomerName(customerName)
            }
        } catch (e: Exception) {
            message = "프로필 정보를 불러오지 못했습니다."
        }
    }

    if (isEditingProfile) {
        ProfileEditScreen(
            modifier = modifier,
            profile = EditableProfile(
                name = customerName,
                email = customerEmail,
                phone = customerPhone,
                profileImageUrl = profileImageUrl
            ),
            message = message,
            isSaving = isSaving,
            onBack = { isEditingProfile = false },
            onSave = { draft ->
                coroutineScope.launch {
                    val token = tokenManager.getAccessToken()
                    if (token == null) {
                        message = "로그인이 필요합니다."
                        return@launch
                    }

                    val nextName = draft.name.trim().ifBlank { "고객" }
                    val nextEmail = draft.email.trim()
                    val nextPhone = draft.phone.trim()

                    if (nextEmail.isBlank()) {
                        message = "이메일을 입력해 주세요."
                        return@launch
                    }

                    if (!emailPattern.matches(nextEmail)) {
                        message = "이메일 형식에 맞게 입력해 주세요."
                        return@launch
                    }

                    if (nextPhone.isBlank()) {
                        message = "휴대폰 번호를 입력해 주세요."
                        return@launch
                    }

                    if (!phonePattern.matches(nextPhone)) {
                        message = "휴대폰 번호 형식에 맞게 입력해 주세요."
                        return@launch
                    }

                    val profileChanged =
                        nextName != customerName ||
                            nextEmail != customerEmail ||
                            draft.profileImageUrl != profileImageUrl
                    val phoneChanged = nextPhone != customerPhone

                    if (!profileChanged && !phoneChanged) {
                        isEditingProfile = false
                        return@launch
                    }

                    isSaving = true
                    message = ""

                    try {
                        var updatedName = customerName
                        var updatedEmail = customerEmail
                        var updatedPhone = customerPhone
                        var updatedProfileImageUrl = profileImageUrl

                        if (profileChanged) {
                            val profileResponse = ApiClient.authApi.updateMyProfile(
                                authorization = "Bearer $token",
                                request = CustomerProfileUpdateRequest(
                                    name = nextName,
                                    email = nextEmail,
                                    profileImageUrl = draft.profileImageUrl
                                )
                            )

                            if (!profileResponse.isSuccessful || profileResponse.body() == null) {
                                message = "프로필 수정 실패: ${profileResponse.code()}"
                                return@launch
                            }

                            val profile = profileResponse.body()!!
                            updatedName = profile.name.ifBlank { "고객" }
                            updatedEmail = profile.email
                            updatedPhone = profile.phone.orEmpty()
                            updatedProfileImageUrl = profile.profileImageUrl
                        }

                        if (phoneChanged) {
                            val phoneResponse = ApiClient.authApi.updateMyPhone(
                                authorization = "Bearer $token",
                                request = CustomerPhoneUpdateRequest(phone = nextPhone)
                            )

                            if (!phoneResponse.isSuccessful || phoneResponse.body() == null) {
                                message = "휴대폰 번호 수정 실패: ${phoneResponse.code()}"
                                return@launch
                            }

                            val profile = phoneResponse.body()!!
                            updatedName = profile.name.ifBlank { "고객" }
                            updatedEmail = profile.email
                            updatedPhone = profile.phone.orEmpty()
                            updatedProfileImageUrl = profile.profileImageUrl
                        }

                        customerName = updatedName
                        customerEmail = updatedEmail
                        customerPhone = updatedPhone
                        profileImageUrl = updatedProfileImageUrl
                        tokenManager.saveCustomerName(customerName)
                        isEditingProfile = false
                    } catch (e: Exception) {
                        message = "서버 연결 실패: ${e.message}"
                    } finally {
                        isSaving = false
                    }
                }
            }
        )
    } else {
        MyPageHomeScreen(
            modifier = modifier,
            customerName = customerName,
            profileImageUrl = profileImageUrl,
            onEditProfile = {
                message = ""
                isEditingProfile = true
            },
            onLogout = {
                tokenManager.clearAccessToken()
                onLogout()
            }
        )
    }
}

@Composable
private fun MyPageHomeScreen(
    modifier: Modifier,
    customerName: String,
    profileImageUrl: String?,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "내정보",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(34.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAvatar(
                text = "",
                size = 72,
                imageUri = profileImageUrl
            )

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = "${customerName}님",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Surface(
                modifier = Modifier.clickable { onEditProfile() },
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = "✎ 수정",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryBox("주문", "빠른 픽업", Modifier.weight(1f))
            SummaryBox("포인트", "준비 중", Modifier.weight(1f))
            SummaryBox("스탬프", "준비 중", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SoftOrange
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "자주 쓰는 주문을 더 빠르게",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "카드를 미리 등록하는 기능은 MVP 범위 밖이라 제외했습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "마이 페이지",
            style = MaterialTheme.typography.titleSmall,
            color = Muted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyPageRow("내 주문내역", "주문내역 탭에서 확인")
        MyPageRow("자주 가는 매장", "자주가요 탭에서 확인")
        MyPageRow("알림 설정", "준비 중")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PassOrange)
        ) {
            Text("로그아웃")
        }
    }
}

@Composable
private fun ProfileEditScreen(
    modifier: Modifier,
    profile: EditableProfile,
    message: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (EditableProfile) -> Unit
) {
    val context = LocalContext.current
    var draftName by remember(profile.name) { mutableStateOf(profile.name) }
    var draftEmail by remember(profile.email) { mutableStateOf(profile.email) }
    var draftPhone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var draftProfileImageUrl by remember(profile.profileImageUrl) {
        mutableStateOf(profile.profileImageUrl)
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        draftProfileImageUrl = uri.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(enabled = !isSaving) { onBack() }
                    .padding(10.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "프로필 수정",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold
            )

            TextButton(
                onClick = {
                    onSave(
                        EditableProfile(
                            name = draftName,
                            email = draftEmail,
                            phone = draftPhone,
                            profileImageUrl = draftProfileImageUrl
                        )
                    )
                },
                enabled = !isSaving,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = if (isSaving) "저장 중" else "완료",
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "프로필 정보",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(42.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileAvatar(
                text = "",
                size = 128,
                imageUri = draftProfileImageUrl
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "프로필 사진 수정",
                modifier = Modifier.clickable(enabled = !isSaving) {
                    imagePickerLauncher.launch(arrayOf("image/*"))
                },
                style = MaterialTheme.typography.titleMedium,
                color = PassOrange,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(56.dp))

        ProfileTextField("닉네임", draftName) { draftName = it }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileTextField("이메일", draftEmail) { draftEmail = it }

        Spacer(modifier = Modifier.height(16.dp))

        ProfileTextField("휴대폰", draftPhone) { draftPhone = it }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    text: String,
    size: Int,
    imageUri: String? = null
) {
    val context = LocalContext.current
    val imageBitmap = remember(imageUri) {
        if (imageUri.isNullOrBlank()) {
            null
        } else {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(imageUri)).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF9EBBD5)),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "프로필 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            return@Box
        }

        Box(
            modifier = Modifier
                .size((size * 0.28f).dp)
                .clip(CircleShape)
                .align(Alignment.Center)
                .background(Color(0xFFD7E6F2))
        )

        Box(
            modifier = Modifier
                .size(width = (size * 0.68f).dp, height = (size * 0.34f).dp)
                .clip(RoundedCornerShape(topStartPercent = 60, topEndPercent = 60))
                .align(Alignment.BottomCenter)
                .background(Color(0xFFD7E6F2))
        )

        if (size <= 80) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummaryBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = PassOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MyPageRow(
    title: String,
    trailing: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = trailing,
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.width(86.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}
