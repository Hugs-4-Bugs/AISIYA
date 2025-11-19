package code.withHarry.properties;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "perplexity.api")
public class MpAIproperties {
	 private String key;
	    private String url;
}
