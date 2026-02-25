package org.pkaq.sys.notice.ctrl;

import org.pkaq.core.mvc.ctrl.Ctrl;
import org.pkaq.core.mvc.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author PKAQ
 */
@RestController
public class NoticeCtrl extends Ctrl {

    @GetMapping("notices")
    public Response<String> notices() {
        String json = "[" +
                "    {" +
                "      id: '000000001'," +
                "      avatar: 'https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png'," +
                "      title: '你收到了 14 份新周报'," +
                "      datetime: '2017-08-09'," +
                "      type: 'notification'," +
                "    }," +
                "    {" +
                "      id: '000000002'," +
                "      avatar: 'https://gw.alipayobjects.com/zos/rmsportal/OKJXDXrmkNshAMvwtvhu.png'," +
                "      title: '你推荐的 曲妮妮 已通过第三轮面试'," +
                "      datetime: '2017-08-08'," +
                "      type: 'notification'," +
                "    }]";
        return success(json);
    }
}
