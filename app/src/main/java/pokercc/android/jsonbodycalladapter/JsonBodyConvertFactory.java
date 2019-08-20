package pokercc.android.jsonbodycalladapter;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.http.Part;

/**
 * Json格式的body转化器工厂类
 *
 * @author pokercc
 * @time 2019.08.20
 */
public final class JsonBodyConvertFactory extends Converter.Factory {


    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
//        if (type != String.class) {
//            return null;
//        }
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
                return new Converter<Object, RequestBody>() {

                    @Override
                    public RequestBody convert(Object value) throws IOException {
                        return new JsonPartRequestBody(finalKey, value == null ? null : value.toString());
                    }
                };
            }
        }

        return null;

    }

    static final class JsonPartRequestBody extends RequestBody {
        private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8; jsonBody=true");


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

}
