package pokercc.android.jsonbodycalladapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class JsonBodyConvertFactoryTest {
    private interface LoginService {
        @JsonBodyEncoded
        @POST("/login")
        @Multipart
        Call<ResponseBody> login(
                @Part("username") String username,
                @Part("password") String password
        );

        @GET("/")
        Call<ResponseBody> sampleGet(
                @Query("username") String username,
                @Query("password") String password
        );


    }

    private LoginService loginService;

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {


        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse()
                .setBody("Login success!")
        );

        loginService = new Retrofit.Builder()
                .addConverterFactory(new JsonBodyConvertFactory())
                .baseUrl(server.url("/"))
                .callFactory(new OkHttpClient.Builder()
                        .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(8888)))
                        .addInterceptor(new JsonBodyInterceptor())
                        .build())

                .build()
                .create(LoginService.class);

    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void testIfWork() throws IOException {
        Call<ResponseBody> call = loginService.login("pokercc", "123456");
        String result = call.execute().body().string();
//        String result = responseBody.string();
        System.out.println("result:" + result);

    }
    @Test
    public void testSampleGet() throws IOException {
        Call<ResponseBody> call = loginService.sampleGet("pokercc", "123456");
        String result = call.execute().body().string();
        System.out.println("result:" + result);

    }
}