package com.ev.service.user;

import java.io.File;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;
import com.ev.dto.member.EvMemberUpdateDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvMemberServiceImpl implements EvMemberService {

    private final EvMemberDAO evMemberDAO;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate stringRedisTemplate;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^010-\\d{4}-\\d{4}$");

    private static final String DEFAULT_PROFILE_IMAGE_URL =
            "/images/member/profile/default-profile.png";

    private static final String PROFILE_UPLOAD_URL_PREFIX =
            "/upload/member/profile/";

    private static final String EMAIL_CODE_KEY_PREFIX =
            "auth:email-code:";

    private static final String EMAIL_VERIFIED_KEY_PREFIX =
            "auth:email-verified:";

    @Override
    @Transactional
    public void join(EvMemberDTO evMemberDTO, MultipartFile profileImage) throws Exception {
        log.info("@# EvMemberServiceImpl.join()");
        log.info("@# join userId => {}", evMemberDTO.getUserId());

        if (!PASSWORD_PATTERN.matcher(evMemberDTO.getPassword()).matches()) {
            throw new IllegalArgumentException("비밀번호는 영문자, 숫자, 특수문자를 포함한 8~20자로 입력하세요.");
        }

        if (evMemberDTO.getPhone() != null
                && !evMemberDTO.getPhone().isBlank()
                && !PHONE_PATTERN.matcher(evMemberDTO.getPhone()).matches()) {
            throw new IllegalArgumentException("휴대폰 번호는 010-0000-0000 형식으로 입력하세요.");
        }

        if (evMemberDAO.countByUserId(evMemberDTO.getUserId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (evMemberDAO.countByEmail(evMemberDTO.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (evMemberDTO.getPhone() != null
                && !evMemberDTO.getPhone().isBlank()
                && evMemberDAO.countByPhone(evMemberDTO.getPhone()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        if (evMemberDAO.countByNickname(evMemberDTO.getNickname()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 이메일 인증 여부 최종 확인
        String verifiedKey = EMAIL_VERIFIED_KEY_PREFIX + evMemberDTO.getEmail();
        String verifiedValue = stringRedisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verifiedValue)) {
            throw new IllegalArgumentException("이메일 인증을 완료해 주세요.");
        }

        // 프로필 이미지 저장
        String profileImageUrl = saveProfileImage(profileImage);
        log.info("@# profileImageUrl => {}", profileImageUrl);

        evMemberDTO.setProfileImageUrl(profileImageUrl);

        // 비밀번호 암호화
        evMemberDTO.setPassword(passwordEncoder.encode(evMemberDTO.getPassword()));

        evMemberDTO.setUserType("USER");
        evMemberDTO.setLoginType("LOCAL");
        evMemberDTO.setStatus("ACTIVE");

        evMemberDAO.insertMember(evMemberDTO);

        // 회원가입 완료 후 인증 키 제거
        stringRedisTemplate.delete(verifiedKey);

        log.info("@# 회원가입 완료 userId => {}", evMemberDTO.getUserId());
    }

    @Override
    public EvMemberDTO findByMemberId(Long memberId) {
        log.info("@# EvMemberServiceImpl.findByMemberId()");
        return evMemberDAO.findByMemberId(memberId);
    }

    @Override
    @Transactional
    public void updateMember(
            EvMemberUpdateDTO updateDTO,
            MultipartFile profileImage) throws Exception {

        log.info("@# EvMemberServiceImpl.updateMember()");
        log.info("@# memberId => {}", updateDTO.getMemberId());

        if (updateDTO.getNickname() == null || updateDTO.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력하세요.");
        }

        EvMemberDTO savedMember =
                evMemberDAO.findByMemberId(updateDTO.getMemberId());

        if (savedMember == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        boolean requestPasswordChange =
                hasText(updateDTO.getCurrentPassword())
                || hasText(updateDTO.getNewPassword())
                || hasText(updateDTO.getNewPasswordConfirm());

        if (requestPasswordChange) {

            if (!"LOCAL".equals(savedMember.getLoginType())) {
                throw new IllegalArgumentException("소셜 로그인 회원은 비밀번호를 변경할 수 없습니다.");
            }

            if (!hasText(updateDTO.getCurrentPassword())) {
                throw new IllegalArgumentException("현재 비밀번호를 입력하세요.");
            }

            if (!passwordEncoder.matches(
                    updateDTO.getCurrentPassword(),
                    savedMember.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (!hasText(updateDTO.getNewPassword())
                    || !PASSWORD_PATTERN.matcher(updateDTO.getNewPassword()).matches()) {
                throw new IllegalArgumentException("새 비밀번호는 영문자, 숫자, 특수문자를 포함한 8~20자로 입력하세요.");
            }

            if (!updateDTO.getNewPassword().equals(updateDTO.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
            }

            updateDTO.setPassword(
                    passwordEncoder.encode(updateDTO.getNewPassword())
            );

        } else {
            updateDTO.setPassword(null);
        }

        // 새 이미지가 있으면 변경, 없으면 기존 이미지 유지
        String profileImageUrl = saveProfileImageForUpdate(profileImage);

        if (profileImageUrl != null) {
            updateDTO.setProfileImageUrl(profileImageUrl);
        }

        evMemberDAO.updateMember(updateDTO);

        log.info("@# 회원정보 수정 완료 memberId => {}", updateDTO.getMemberId());
    }

    @Override
    public boolean isUserIdAvailable(String userId) {
        log.info("@# EvMemberServiceImpl.isUserIdAvailable()");
        return evMemberDAO.countByUserId(userId) == 0;
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        log.info("@# EvMemberServiceImpl.isNicknameAvailable()");

        if (nickname == null || nickname.isBlank()) {
            return false;
        }

        return evMemberDAO.countByNickname(nickname) == 0;
    }

    @Override
    public boolean isEmailAvailable(String email) {
        log.info("@# EvMemberServiceImpl.isEmailAvailable()");

        if (email == null || email.isBlank()) {
            return false;
        }

        return evMemberDAO.countByEmail(email) == 0;
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        log.info("@# EvMemberServiceImpl.isPhoneAvailable()");

        if (phone == null || phone.isBlank()) {
            return false;
        }

        return evMemberDAO.countByPhone(phone) == 0;
    }

    @Override
    public void sendEmailCode(String email) {
        log.info("@# EvMemberServiceImpl.sendEmailCode()");
        log.info("@# email => {}", email);

        if (!hasText(email)) {
            throw new IllegalArgumentException("이메일을 입력하세요.");
        }

        if (evMemberDAO.countByEmail(email) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        String code = createEmailCode();

        String codeKey = EMAIL_CODE_KEY_PREFIX + email;
        String verifiedKey = EMAIL_VERIFIED_KEY_PREFIX + email;

        stringRedisTemplate.delete(verifiedKey);

        stringRedisTemplate.opsForValue().set(
                codeKey,
                code,
                Duration.ofMinutes(3)
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[EV Charge] 이메일 인증번호");
        message.setText(
                "EV Charge 회원가입 이메일 인증번호입니다.\n\n"
                + "인증번호: " + code + "\n\n"
                + "인증번호는 3분간 유효합니다."
        );

        javaMailSender.send(message);

        log.info("@# 이메일 인증번호 발송 완료 email => {}", email);
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        log.info("@# EvMemberServiceImpl.verifyEmailCode()");
        log.info("@# email => {}", email);

        if (!hasText(email) || !hasText(code)) {
            return false;
        }

        String codeKey = EMAIL_CODE_KEY_PREFIX + email;
        String savedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (!code.equals(savedCode)) {
            return false;
        }

        String verifiedKey = EMAIL_VERIFIED_KEY_PREFIX + email;

        stringRedisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                Duration.ofMinutes(10)
        );

        stringRedisTemplate.delete(codeKey);

        log.info("@# 이메일 인증 성공 email => {}", email);

        return true;
    }

    private String saveProfileImage(MultipartFile profileImage) throws Exception {

        if (profileImage == null || profileImage.isEmpty()) {
            return DEFAULT_PROFILE_IMAGE_URL;
        }

        String originalName = profileImage.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            return DEFAULT_PROFILE_IMAGE_URL;
        }

        validateImageFile(originalName);

        String savedName = saveImageFile(profileImage, originalName);

        return PROFILE_UPLOAD_URL_PREFIX + savedName;
    }

    private String saveProfileImageForUpdate(MultipartFile profileImage) throws Exception {

        if (profileImage == null || profileImage.isEmpty()) {
            return null;
        }

        String originalName = profileImage.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            return null;
        }

        validateImageFile(originalName);

        String savedName = saveImageFile(profileImage, originalName);

        return PROFILE_UPLOAD_URL_PREFIX + savedName;
    }

    private void validateImageFile(String originalName) {

        String lowerName = originalName.toLowerCase();

        if (!(lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".gif"))) {
            throw new IllegalArgumentException("프로필 이미지는 jpg, jpeg, png, gif 파일만 업로드할 수 있습니다.");
        }
    }

    private String saveImageFile(
            MultipartFile profileImage,
            String originalName) throws Exception {

        String uploadDirPath = System.getProperty("user.dir")
                + "/upload/member/profile";

        File uploadDir = new File(uploadDirPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String savedName = UUID.randomUUID() + "_" + originalName;
        File savedFile = new File(uploadDir, savedName);

        log.info("@# uploadDirPath => {}", uploadDirPath);
        log.info("@# savedFile path => {}", savedFile.getAbsolutePath());

        profileImage.transferTo(savedFile);

        return savedName;
    }

    private String createEmailCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);

        return String.valueOf(code);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}