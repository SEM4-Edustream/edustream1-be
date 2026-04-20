package sem4.edustreambe.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sem4.edustreambe.entity.Category;
import sem4.edustreambe.repository.CategoryRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInitializer implements CommandLineRunner {

    CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Initializing default categories...");
            
            List<Category> categories = List.of(
                Category.builder().name("Development").slug("development").description("Web, Mobile, Game Development").build(),
                Category.builder().name("Business").slug("business").description("Finance, Management, Strategy").build(),
                Category.builder().name("IT & Software").slug("it-software").description("Network, Security, Operating Systems").build(),
                Category.builder().name("Design").slug("design").description("Graphic Design, UI/UX, Web Design").build(),
                Category.builder().name("Marketing").slug("marketing").description("Digital Marketing, SEO, Social Media").build(),
                Category.builder().name("Personal Development").slug("personal-development").description("Leadership, Happiness, Career").build()
            );
            
            categoryRepository.saveAll(categories);
            log.info("Finished initializing {} default categories.", categories.size());
        }
    }
}
