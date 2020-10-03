package net.webtide.github.releasedrafter;

import net.webtide.github.releasedrafter.release.Category;
import net.webtide.github.releasedrafter.release.ReleaseDraft;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReleaseDraftTest
{

    @Test
    public void simpleread() throws IOException
    {
        Path releasePath = MavenTestingUtils.getTestResourcePathFile( "release-draft/release-config.yml");
        try (InputStream inputStream = Files.newInputStream(releasePath)){
            ReleaseDraft releaseDraft = ReleaseDraft.load(inputStream);
            System.out.println("releaseDraft:" + releaseDraft);
            assertNotNull( releaseDraft, "releaseDraft should not be null");
            assertThat(releaseDraft.getCategories().size(), is(12));
            Category category =
                releaseDraft.getCategories().stream()
                    .filter( c -> c.getLabels().stream()
                        .filter( s -> s.equals( "documentation" ) ).count()==1)
                    .collect(Collectors.toList() ).get( 0 );
            assertThat(category.getTitle(), containsString("Documentation updates"));

            category =
                releaseDraft.getCategories().stream()
                    .filter( c -> c.getLabels().stream()
                        .filter( s -> s.equals( "enhancement" ) ).count()==1)
                    .collect(Collectors.toList() ).get( 0 );
            assertThat(category.getTitle(), containsString("New features and improvements"));

        }
    }
}
