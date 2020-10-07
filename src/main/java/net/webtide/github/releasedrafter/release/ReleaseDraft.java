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

package net.webtide.github.releasedrafter.release;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReleaseDraft
{
    private List<Category> categories;

    //@JsonAlias("exclude-labels")
    private List<String> excludeLabels;

    public ReleaseDraft()
    {
    }

    public List<Category> getCategories()
    {
        return categories;
    }

    public void setCategories(List<Category> categories)
    {
        this.categories = categories;
    }

    public List<String> getExcludeLabels()
    {
        return excludeLabels;
    }

    public void setExcludeLabels(List<String> excludeLabels)
    {
        this.excludeLabels = excludeLabels;
    }

    @Override
    public String toString()
    {
        return "ReleaseDraft{" + "categories=" + categories + ", excludeLabels=" + excludeLabels + '}';
    }

    public static ReleaseDraft load(InputStream inputStream)
        throws IOException
    {
        Constructor constructor = new Constructor(ReleaseDraft.class);
        TypeDescription typeDescription = new TypeDescription( ReleaseDraft.class );
        typeDescription.substituteProperty( "exclude-labels", List.class,
                                            "getExcludeLabels", "setExcludeLabels");
        constructor.addTypeDescription( typeDescription );
        Yaml yaml = new Yaml(constructor);
        return yaml.load( inputStream );
    }
}
