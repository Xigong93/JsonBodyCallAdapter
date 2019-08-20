package pokercc.android.jsonbodycalladapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Json格式的body转化器工厂类
 * @author pokercc
 * @time 2019.08.20
 */
class JsonBodyConvertFactory extends Converter.Factory {
    /**
     * Json格式的Http Body
     */
    @Retention(RUNTIME)
    @interface JsonBodyEncoded {

    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        // 记录参数存到Head里
        for (Annotation parameterAnnotation : methodAnnotations) {
            if (parameterAnnotation instanceof JsonBodyEncoded) {
                final Converter<Object, RequestBody> requestBodyConverter = retrofit.requestBodyConverter(type, parameterAnnotations, methodAnnotations);
                return new Converter<Object, RequestBody>() {

                    @Override
                    public RequestBody convert(Object value) throws IOException {
                        final RequestBody realRequestBody = requestBodyConverter.convert(value);
                        return new JsonRequestBody(realRequestBody);
                    }
                };
            }
        }

        return null;

    }


    /**
     * Json格式的表单
     */
    private final static class JsonRequestBody extends RequestBody {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
        private static final Charset UTF_8 = Charset.forName("UTF-8");
        private final RequestBody originRequestBody;

        JsonRequestBody(RequestBody originRequestBody) {
            this.originRequestBody = originRequestBody;
        }


        @Override
        public MediaType contentType() {
            return MEDIA_TYPE;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            // 这其实是post的标准形式的body，需要转化成JsonBody

            // 读出原body
            final Buffer buffer = new Buffer();
            this.originRequestBody.writeTo(buffer);
            String body = buffer.readString(UTF_8);
            if (body.isEmpty()) {
                return;
            }
            // 专成json格式的body
            String jsonBody = null;
            try {
                jsonBody = toJsonBody(body);
            } catch (JSONException e) {
                throw new IOException(e);
            }
            // 写入流中
            if (jsonBody != null && !jsonBody.isEmpty()) {
                sink.writeUtf8(jsonBody);
            }
        }

        /**
         * 表单格式的body转json格式的body
         *
         * @param body
         * @return
         * @throws JSONException
         */
        private String toJsonBody(String body) throws JSONException {
            if (body == null || body.isEmpty()) {
                return null;
            }
            JSONObject jsonObject = new JSONObject();
            String[] pairStr = body.split("&");
            for (String pair : pairStr) {
                String[] kv = pair.split("=");
                jsonObject.putOpt(kv[0], kv.length > 1 ? kv[1] : null);
            }

            return jsonObject.toString();
        }
    }
}
