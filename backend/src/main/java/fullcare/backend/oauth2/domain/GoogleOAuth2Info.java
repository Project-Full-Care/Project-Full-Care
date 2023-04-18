package fullcare.backend.oauth2.domain;

import java.util.Map;

// todo 각 getter별 NPE 방지용 검증코드가 필요함
public class GoogleOAuth2Info extends OAuth2UserInfo{

    public GoogleOAuth2Info(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {

        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
}
