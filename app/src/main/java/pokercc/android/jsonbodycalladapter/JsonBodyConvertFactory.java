package pokercc.android.jsonbodycalladapter;


import com.google.gson.JsonObject;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.Part;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Json格式的body转化器工厂类
 *
 * @author pokercc
 * @time 2019.08.20
 */
public final class JsonBodyConvertFactory extends Converter.Factory {
    /**
     * Json格式的Http Body
     */
    @Retention(RUNTIME)
    @interface JsonBodyEncoded {

    }


    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type != String.class) {
            return null;
        }
        for (Annotation annotation : methodAnnotations) {
            if (annotation instanceof JsonBodyEncoded) {

                String key = null;
                for (Annotation parameterAnnotation : parameterAnnotations) {
                    if (parameterAnnotation instanceof Part) {
                        key = ((Part) parameterAnnotation).value();
                    }
                }
                final String finalKey = key;
                if (finalKey == null || finalKey.isEmpty()) {
                    return null;
                }
                return new Converter<String, RequestBody>() {

                    @Override
                    public RequestBody convert(String value) throws IOException {
                        return new JsonPartRequestBody(finalKey, value);
                    }
                };
            }
        }

        return null;

    }

    private static final class JsonPartRequestBody extends RequestBody {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8; jsonBody=true");
        private static final Charset UTF_8 = Charset.forName("UTF-8");
        public final String key;
        public final String value;

        private JsonPartRequestBody(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public MediaType contentType() {
            return MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink bufferedSink) throws IOException {


        }
    }

    public final static class JsonBodyInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            RequestBody requestBody = request.body();
            if (requestBody instanceof MultipartBody) {
                List<MultipartBody.Part> parts = ((MultipartBody) requestBody).parts();
                JsonObject jsonObject = null;
                for (MultipartBody.Part part : parts) {
                    RequestBody body = part.body();
                    if (body instanceof JsonPartRequestBody) {
                        if (jsonObject == null) jsonObject = new JsonObject();
                        jsonObject.addProperty(((JsonPartRequestBody) body).key, ((JsonPartRequestBody) body).value);

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
    }

    /**
     * Json格式的表单
     */
    private final static class JsonRequestBody extends RequestBody {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = Charset.forName("UTF-8");
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
