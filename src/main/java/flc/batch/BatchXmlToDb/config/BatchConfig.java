package flc.batch.BatchXmlToDb.config;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import flc.batch.BatchXmlToDb.model.Person;
import flc.batch.BatchXmlToDb.processor.PersonItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public StaxEventItemReader<Person> reader() {
            StaxEventItemReader<Person> reader = new StaxEventItemReader<Person>();
            reader.setResource(new ClassPathResource("persons.xml"));
            reader.setFragmentRootElementName("person");

            Map<String, String> aliasesMap = new HashMap<String, String>();
            aliasesMap.put("person", "flc.batch.BatchXmlToDb.model.Person");

            XStreamMarshaller marshaller = new XStreamMarshaller();
            marshaller.setAliases(aliasesMap);

            reader.setUnmarshaller(marshaller);
            return reader;

}   

    @Bean
    public JdbcBatchItemWriter<Person> writer() {
            JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
            writer.setDataSource(dataSource);
            writer.setSql("INSERT INTO person(person_id,first_name,last_name,email,age) VALUES (?,?,?,?,?)");
            writer.setItemPreparedStatementSetter(new PersonPreparedStatementSetter()); 
            return writer;    
        
    }       

    @Bean
    public Step step1() {
        return stepBuilderFactory
                    .get("step1")
                    .<Person,Person>chunk(100)
                    .reader(reader())
                    .processor(processor())
                    .writer(writer())
                    .build();
    }


    @Bean
    public Job exportPersonJob() {
        return jobBuilderFactory
                .get("importPersponJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build(); 

    }                    
               
}
