package pokercc.android.jsonbodycalladapter;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * 用户生成json格式的表单的拦截器
 *
 * @author pokercc
 */
public final class JsonBodyInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        if (requestBody instanceof MultipartBody) {
            List<MultipartBody.Part> parts = ((MultipartBody) requestBody).parts();
            JsonObject jsonObject = null;
            for (MultipartBody.Part part : parts) {
                RequestBody body = part.body();
                if (body instanceof JsonBodyConvertFactory.JsonPartRequestBody) {
                    if (jsonObject == null) jsonObject = new JsonObject();
                    jsonObject.addProperty(((JsonBodyConvertFactory.JsonPartRequestBody) body).key, ((JsonBodyConvertFactory.JsonPartRequestBody) body).value);

                }
            }

            if (jsonObject != null) {
                return chain.proceed(request.newBuilder()
                        .method(request.method(), new JsonRequestBody(jsonObject))
                        .build());
            }
        }


        return chain.proceed(request);
    }

    /**
     * Json格式的表单
     */
    private final static class JsonRequestBody extends RequestBody {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

        private final JsonObject jsonObject;

        private JsonRequestBody(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }


        @Override
        public MediaType contentType() {
            return MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.writeUtf8(jsonObject.toString());
        }


    }
}
