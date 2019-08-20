package pokercc.android.jsonbodycalladapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.POST;

public class JsonBodyConvertFactoryTest {
    private interface LoginService {
        @JsonBodyConvertFactory.JsonBodyEncoded
        @POST("/login")
        Call<ResponseBody> login(
                @Field("username") String username,
                @Field("password") String password
        );
    }

    private LoginService loginService;

    @Before
    public void setUp() throws Exception {
        loginService = new Retrofit.Builder()
                .addConverterFactory(new JsonBodyConvertFactory())
                .build()
                .create(LoginService.class);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIfWork() {

    }
}