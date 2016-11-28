package com.boundary.dropwizard.consul.config;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.io.ByteStreams;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An {@link ConfigurationSourceProvider} backed by a Freemarker template
 */
public class TemplateSourceProvider implements ConfigurationSourceProvider {

    private final ConfigurationSourceProvider delegate;
    private final ConsulLookup consulLookup;
    //private final Configuration cfg;

    public TemplateSourceProvider(ConfigurationSourceProvider delegate, ConsulLookup consulLookup) {
        this.delegate = delegate;
        this.consulLookup = consulLookup;

       // cfg = new Configuration(Configuration.VERSION_2_3_25);
        //cfg.setClassForTemplateLoading(this.getClass(), "/");
       // cfg.
      //  cfg.setDefaultEncoding("UTF-8");
     //   cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public InputStream open(String path) throws IOException {
        System.out.println("opening config...");


        try (final InputStream in = delegate.open(path)) {
          //  final String configString = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
          /*  Template t = new Template(path, new InputStreamReader(in), cfg);
            PipedInputStream pin = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(pin);
            try(OutputStreamWriter writer = new OutputStreamWriter(out)) {
                t.process(consulLookup, writer);
            }
            final String config = new String(ByteStreams.toByteArray(pin), StandardCharsets.UTF_8);
            System.out.println(config);*/


          /*  PipedInputStream pin = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(pin);
            try(OutputStreamWriter writer = new OutputStreamWriter(out)) {
                MustacheFactory mf = new DefaultMustacheFactory();
                Mustache mustache = mf.compile(new InputStreamReader(in), path);
                mustache.execute(writer, consulLookup);
                writer.flush();
            }
            final String config = new String(ByteStreams.toByteArray(pin), StandardCharsets.UTF_8);
            System.out.println(config);
            // todo can be more elegant
            return new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));*/

            EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder
                    .configuration()
                    .functions()
                    .add(consulLookup.kv())
                    .add(consulLookup.health_service())
                    .and()
                    .build();

            final String configString = new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8);
            JtwigTemplate template = JtwigTemplate.inlineTemplate(configString, configuration);

            PipedInputStream pin = new PipedInputStream();

            try(PipedOutputStream out = new PipedOutputStream(pin)) {
                JtwigModel model = JtwigModel.newModel(consulLookup);
                template.render(model, out);
                out.flush();
            }
            final String config = new String(ByteStreams.toByteArray(pin), StandardCharsets.UTF_8);
            System.out.println(config);
            // todo can be more elegant
            return new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
        }
    }
}
