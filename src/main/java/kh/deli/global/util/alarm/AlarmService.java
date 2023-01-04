package kh.deli.global.util.alarm;

import com.google.gson.Gson;
import kh.deli.global.entity.NoticeDTO;
import kh.deli.global.entity.UserType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

@Service
@AllArgsConstructor
public class AlarmService {
    private final AlarmNoticeMapper noticeMapper;
    private final AlarmAccountMapper accountMapper;
    private final AlarmMemberMapper memberMapper;
    private final AlarmOwnerMapper ownerMapper;
    private final Gson gson;

    public List<Integer> getSeqListByType(UserType userType) {
        Optional<List<Integer>> seqList = Optional.ofNullable(
                accountMapper.getSeqListByType(userType.getType()));

        return seqList.orElse(new ArrayList<>());
    }

    /**
     * @return Not Null
     */
    public UserType getUserType(Integer accSeq) {
        String type = Optional.ofNullable(accountMapper.getType(accSeq))
                .orElse("");
        switch (type) {
            case "client":
                return UserType.MEMBER;
            case "business":
                return UserType.OWNER;
            case "admin":
                return UserType.ADMIN;
            default:
                return UserType.NONE;
//                throw new RuntimeException("로그인이 필요합니다.");
        }
    }

    public String saveNotice(NoticeDTO notice) {
        notice.setNotice_seq(noticeMapper.getNextSeq());
        noticeMapper.put(notice);

        String type = accountMapper.getType(notice.getFrom_acc_seq());
        String name = "";

        if (type.equals(UserType.ADMIN.getType())) {
            name = "Admin";
        } else if (type.equals(UserType.MEMBER.getType())) {
            name = ownerMapper.getName(notice.getFrom_acc_seq());
        } else if (type.equals(UserType.OWNER.getType())) {
            name = memberMapper.getNick(notice.getFrom_acc_seq());
        }

        NoticeResponseDTO response = NoticeResponseDTO.builder()
                .notice_seq(notice.getNotice_seq())
                .from_name(name)
                .notice_title(notice.getNotice_title())
                .notice_content(notice.getNotice_content())
                .notice_time(notice.getNotice_time())
                .build();

        String sendText = gson.toJson(response);

        return sendText;
    }

    public String getSendText(NoticeDTO notice, UserType userType) {
        String text = "";

        NoticeResponseDTO responseDto = NoticeResponseDTO.builder()
                .notice_seq(notice.getNotice_seq())
                .from_name(ownerMapper.getName(notice.getFrom_acc_seq()))
                .notice_title(notice.getNotice_title())
                .notice_content(notice.getNotice_content())
                .notice_time(notice.getNotice_time())
                .build();
        switch (userType) {
            case MEMBER:
                responseDto.setFrom_name(memberMapper.getNick(notice.getFrom_acc_seq()));
                break;
            case OWNER:
                responseDto.setFrom_name(ownerMapper.getName(notice.getFrom_acc_seq()));
                break;
            case ADMIN:
                responseDto.setFrom_name("시스템관리자");
                break;
        }

        return text;
    }

    public void sendTextOnType(NoticeDTO notice, Set<Session> clientsCopy, UserType userType) throws IOException {
        for (Session client : clientsCopy) {
            Map<String, Object> clientProperties = client.getUserProperties();

            HttpSession hSession = (HttpSession) clientProperties.get("httpSession");
            Integer toAccSeq = (Integer) hSession.getAttribute("acc_seq");

            notice.setTo_acc_seq(toAccSeq);

            String msg = this.getSendText(notice, userType);

            client.getBasicRemote().sendText(msg);
        }
    }

    public List<Integer> insertNoticeOnType(NoticeDTO notice, UserType userType) {
        List<Integer> seqList = this.getSeqListByType(userType);
        List<Integer> noticeSeqList = new ArrayList<>();
        for (int seq : seqList) {
            Integer newNoticeSeq = noticeMapper.getNextSeq();
            notice.setNotice_seq(newNoticeSeq);
            notice.setTo_acc_seq(seq);
            noticeMapper.put(notice);

            noticeSeqList.add(newNoticeSeq);
        }

        return noticeSeqList;
    }

    public void delete(Integer seq) {
        noticeMapper.delete(seq);
    }

    public List<NoticeDTO> findNoticeByToSeq(int toAccSeq) {
        Optional<List<NoticeDTO>> noticeList
                = Optional.ofNullable(noticeMapper.findByToSeq(toAccSeq));
        return noticeList.orElse(new ArrayList<>());
    }
}
