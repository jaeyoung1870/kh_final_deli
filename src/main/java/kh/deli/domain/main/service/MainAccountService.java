package kh.deli.domain.main.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kh.deli.domain.main.mapper.MainAccountMapper;
import kh.deli.global.entity.AccountDTO;
import kh.deli.global.entity.AddressDTO;
import kh.deli.global.entity.MemberDTO;
import kh.deli.global.entity.MenuDTO;
import kh.deli.global.util.Encryptor;
import kh.deli.global.util.FileUtil;
import kh.deli.global.util.GenerateRandomCode;
import kh.deli.global.util.naverSensV2.NaverSms;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@AllArgsConstructor
public class MainAccountService {
    private final MainAccountMapper mainAccountMapper;
    private final RestTemplate restTemplate;
    private final GenerateRandomCode generateRandomCode;
    private final HttpSession session;
    private final Gson gson;

    /**
     * <h2>email 중복체크</h2>
     *
     * @param email 검색할 email
     * @return 검색한 email 이 있으면 true, 없으면 false
     */
    public boolean dupleCheck(String email) throws Exception {

        String result = mainAccountMapper.findByEmail(email);

        if (result != null) {
            return true;
        }
        return false;
    }

    /**
     * <h2>Normal Type 로그인</h2>
     * @return COUNT(*)
     */
    public int login(String email, String pw) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put("email", email);
        param.put("pw", Encryptor.getSHA512(pw));
        return mainAccountMapper.login(param);
    }

    /**
     * <h2>회원탈퇴 (Address -> Member -> Account)</h2>
     */
    @Transactional
    public void withdrawal(int accSeq) throws Exception {
        String accType = this.selectType(accSeq);

        if (accType.equals("business")){ // 사업자 탈퇴

            // OwnerSeq 값 불러오기
            Integer ownerSeq = mainAccountMapper.getOwnerSeqByAccSeq(accSeq);

            // 운영중인 StoreSeq 불러오기
            List<Integer> storeSeqList = mainAccountMapper.getStoreSeqListByOwnerSeq(ownerSeq);

            // 삭제할 Img File 리스트
            List<String> deleteReviewImgList = new ArrayList<>();
            List<String> deleteMenuImgList = new ArrayList<>();

            // 삭제할 OwnerCard Img File 명 담기
            String deleteOriginalFile = mainAccountMapper.findOwnerCardBySeq(accSeq);

            for (int i = 0; storeSeqList.size() > i; i++) {
                // 식당 Review Img Data 불러오기
                List<String> reviewImgJsonList = mainAccountMapper.getReviewImgListByStoreSeq(storeSeqList.get(i));

                for (int k = 0; reviewImgJsonList.size() > k; k++) {
                    Type reviewImgListType = new TypeToken<List<String>>(){}.getType();
                    List<String> reviewImgList = (gson.fromJson(reviewImgJsonList.get(k), reviewImgListType));

                    for (int j = 0; reviewImgList.size() > j; j++) {
                        // 리스트에 Review Img File 명 담기
                        deleteReviewImgList.add(reviewImgList.get(j));
                    }
                }

                // 식당 Menu Img Data 불러오기
                List<MenuDTO> menuList = mainAccountMapper.getMenuImgListByStoreSeq(storeSeqList.get(i));

                for (int l = 0; menuList.size() > l; l++) {
                    // 리스트에 Menu Img File 명 담기
                    deleteMenuImgList.add(menuList.get(l).getMenu_img());
                    // Menu_option 테이블 삭제
                    mainAccountMapper.deleteMenuOptionByMenuOption(menuList.get(l).getMenu_seq());
                }

                // Review 테이블 데이터 삭제
                mainAccountMapper.deleteReviewByStoreSeq(storeSeqList.get(i));
                // Dibs 테이블 데이터 삭제
                mainAccountMapper.deleteDibsByStoreSeq(storeSeqList.get(i));
                // Menu 테이블 삭제
                mainAccountMapper.deleteMenuByStoreSeq(storeSeqList.get(i));
            }
            // Store 테이블 데이터 삭제
            mainAccountMapper.deleteStoreByOwnerSeq(ownerSeq);
            // Owner 테이블 데이터 삭제
            mainAccountMapper.deleteOwnerByAccSeq(accSeq);
            // Account 테이블 데이터 삭제
            mainAccountMapper.deleteAccountByAccSeq(accSeq);


            // [ 사진 파일 삭제 ]
            FileUtil fileUtil = new FileUtil();

            // Review Img File 삭제
            for (String reviewImg : deleteReviewImgList) {
                fileUtil.delete(session, "/resources/img/review", reviewImg);
            }
            // Menu Img File 삭제
            for (String menuImg : deleteMenuImgList) {
                fileUtil.delete(session, "/resources/img/menu-img", menuImg);
            }
            // OwnerCard Img File 삭제
            fileUtil.delete(session, "/resources/img/owner-card", deleteOriginalFile);


        }else { // 일반 클라이언트 탈퇴

            // 리뷰 사진 img 파일 삭제
            List<String> reviewImgJsonList = mainAccountMapper.getReviewImgListByAccSeq(accSeq);

            for (int i = 0; reviewImgJsonList.size() > i; i++) {

                List<String> deleteReviewImgList = new ArrayList<>();
                Type reviewImgListType = new TypeToken<List<String>>(){}.getType();
                deleteReviewImgList.add(gson.fromJson(reviewImgJsonList.get(i), reviewImgListType));

                FileUtil fileUtil = new FileUtil();
                for (String reviewImg : deleteReviewImgList) {
                    fileUtil.delete(session, "/resources/img/review", reviewImg);
                }
            }

            // Review 테이블 데이터 삭제
            mainAccountMapper.deleteReviewByAccSeq(accSeq);
            // Dibs 테이블 데이터 삭제
            mainAccountMapper.deleteDibsByAccSeq(accSeq);
            // Member_coupon 테이블 데이터 삭제
            mainAccountMapper.deleteMemberCouponByAccSeq(accSeq);
            // Address 테이블 데이터 삭제
            mainAccountMapper.deleteAddressByAccSeq(accSeq);
            // Member 테이블 데이터 삭제
            mainAccountMapper.deleteMemberByAccSeq(accSeq);
            // Account 테이블 데이터 삭제
            mainAccountMapper.deleteAccountByAccSeq(accSeq);
        }
    }


    /**
     * <h2>카카오 연결헤제</h2>
     * @param accessToken
     */
    public void kakaoUnlink(String accessToken) throws Exception {
        String reqURL = "https://kapi.kakao.com/v1/user/unlink";
        URL url = new URL(reqURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = conn.getResponseCode();
        System.out.println("responseCode : " + responseCode);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String result = "";
        String line = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }
        System.out.println("개좆같은새끼야 : " + result);
    }

    /**
     * member 회원가입 메서드
     */
    @Transactional
    public void memberSignUp(AccountDTO accountDTO,MemberDTO memberDTO,AddressDTO addressDTO) throws Exception {
        int getNextAccSeq = mainAccountMapper.getNextAccSeq();
        accountDTO.setAcc_pw(Encryptor.getSHA512(accountDTO.getAcc_pw()));
        accountDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.memberSignUp(accountDTO);
        memberDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.insertMember(memberDTO);
        addressDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.insertAddress(addressDTO);
    }




    /**
     * 카카오 AccessToken 값 가져오는 메서드
     *
     * @param code
     * @return
     */
    public String getKakaoAccessToken(String code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=1475b617eab69841d5cabd68f1527015"); // TODO REST_API_KEY 입력
            sb.append("&redirect_uri=http://mydeli.me/account/oauth/kakao"); // TODO 인가코드 받은 redirect_uri 입력
            sb.append("&code=" + code);
            bw.write(sb.toString());
            bw.flush();

            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
//            System.out.println("responseCode : " + responseCode);

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
//            System.out.println("response body : " + result);

            //Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();
//            System.out.println("access_token : " + access_Token);
//            System.out.println("refresh_token : " + refresh_Token);
            br.close();
            bw.close();
        } catch (IOException e) {
        }
        return access_Token;
    }

    /**
     * 카카오 회원 ID 값 가져오는 메서드
     *
     * @param code
     * @return
     */
    public String getKakaoId(String code) {
        String myTocken = "Bearer " + code;

        //헤더 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", myTocken);

        //요청 url
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl("https://kapi.kakao.com/v2/user/me");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<String> response = null;

        //요청
        String id = null;
        try {
            response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(response.getBody());
            id = element.getAsJsonObject().get("id").getAsString();
            // System.out.println("아이디 : " + id);

        } catch (HttpStatusCodeException e) {
            // System.out.println("error :" + e);
        }
        return id;
    }

    /**
     * kakaoId 중복체크
     *
     * @param kakaoId 검색할 kakaoId
     * @param kakaoId
     * @return 검색한 kakaoId가 있으면 true, 없으면 false
     * @return
     * @throws Exception
     */
    public boolean dupleCheckKakaoId(String kakaoId) {
        int result = mainAccountMapper.findByAccToken(kakaoId);
        if (result == 1) {
            return true;
        }
        return false;
    }

    /**
     * kakao 회원가입 메서드
     *
     * @param accountDTO
     * @throws Exception
     */
    @Transactional
    public void kakaoSignUp(AccountDTO accountDTO,MemberDTO memberDTO,AddressDTO addressDTO) throws Exception {
        int getNextAccSeq = mainAccountMapper.getNextAccSeq();
        accountDTO.setAcc_pw(Encryptor.getSHA512(accountDTO.getAcc_pw()));
        accountDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.kakaoSignUp(accountDTO);
        memberDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.insertMember(memberDTO);
        addressDTO.setAcc_seq(getNextAccSeq);
        mainAccountMapper.insertAddress(addressDTO);
    }











    public String getAccEmail(String acc_token) {
        return mainAccountMapper.getAccEmail(acc_token);
    }

    public int getAccSeq(String acc_email) {
        return mainAccountMapper.getAccSeq(acc_email);
    }

    /** 연락처 문자 인증 전송
     *
     * @param tel
     * @return
     */
    public String sendRandomMessage(String tel) {
        NaverSms message = new NaverSms();
        Random rand = new Random();
        String numStr = "";
        for (int i = 0; i < 6; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }
        message.send_msg(tel, "딜리본인인증번호 ["+numStr+"]");
        return numStr;
    }

    //MemberMainPage
    public String selectType(int acc_seq){
        return mainAccountMapper.selectType(acc_seq);
    }


    /**
     * 이메일 찾기
     */
    public List<AccountDTO> findAccountByPhoneNumber(String phoneNumber) {
        return mainAccountMapper.findAccountByPhoneNumber(phoneNumber);
    }

    /**
     * 비밀번호 찾기
     */
    public Integer findPassWordByPhoneNumber(String email, String phoneNumber) {
        Map<String, String> param = new HashMap<>();
        param.put("acc_email", email);
        param.put("mem_phone", phoneNumber);
        return mainAccountMapper.findSeqByEmailAndPhone(param);
    }

    /**
     * 임시 비밀번호 발급
     * @return 변경된 임시 비밀번호
     */
    public String modifyPassWordWithRandomCodeBySeq(int accSeq) {
        String randomCode = generateRandomCode.excuteGenerate();
        Map<String, Object> param = new HashMap<>();
        param.put("acc_seq", accSeq);
        param.put("acc_pw", Encryptor.getSHA512(randomCode));
        mainAccountMapper.modifyPassWordWithRandomCodeBySeq(param);
        return randomCode;
    }






}
