//
// ========================================================================
// Copyright (c) Webtide LLC and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package net.webtide.github.releasedrafter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import net.webtide.github.releasedrafter.release.Category;
import net.webtide.github.releasedrafter.release.GHReleaseFinder;
import net.webtide.github.releasedrafter.release.ReleaseDraft;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReleaseDraftTest extends AbstractGitHubTest
{

    Logger LOG = LoggerFactory.getLogger(getClass());

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



    @Test
    public void findReleaseByName() throws IOException {
        GHRepository ghRepository = github.getRepository( "joakime/experiments-with-release-drafter" );
        GHRelease ghRelease = new GHReleaseFinder(ghRepository).findByName("9.4.32");
        LOG.info( "ghRelease: name: {}, tagName: {}, targetCommitish: {}, id: {}",
                  ghRelease.getName(),
                  ghRelease.getTagName(),
                  ghRelease.getTargetCommitish(),
                  ghRelease.getId());

    }


    private static DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
        Extensions.ALL
    );

    static final MutableDataSet FORMAT_OPTIONS = new MutableDataSet();
    static {
        FORMAT_OPTIONS.set(Parser.EXTENSIONS, Parser.EXTENSIONS.get(OPTIONS));
    }

    static final Parser PARSER = Parser.builder(OPTIONS).build();


    @Test
    public void release_body_parser() throws Exception
    {
        Path releaseBodyPath = MavenTestingUtils.getTestResourcePathFile( "release-draft/release-body.txt");
        Document document = PARSER.parse(new String(Files.readAllBytes(releaseBodyPath)));

        document.getChildren().forEach( node -> {
            LOG.info( "node: type {} name {} chars {}",
                                                          node.getClass(),
                                                          node.getNodeName(),
                                                          node.getChars());
            if(node instanceof BulletList )
            {
                node.getChildren().forEach( child -> {
                    LOG.info( "BulletList child: type {} name {} chars {}",
                              child.getClass(),
                              child.getNodeName(),
                              child.getChars() );
                } );
                BulletListItem item = new BulletListItem();
                item.setChars( BasedSequence.of( "FOO NEW CONTENT (#1212)"));
                node.appendChild(item);
            }
        });


        String text = Formatter.builder().build().render(document );
        System.out.println("Body back to text");
        System.out.println(text);
    }

}
