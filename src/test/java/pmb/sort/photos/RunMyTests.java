package pmb.sort.photos;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Retention(RUNTIME)
@Target(TYPE)
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = ReplaceUnderscores.class)
public @interface RunMyTests {}
