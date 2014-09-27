/*
 * HashMemItemProvider
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains

import com.hashmem.jetbrains.command.SyncHashMemCommand
import com.intellij.ide.util.gotoByName.ChooseByNameBase
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import org.jetbrains.annotations.NotNull

class HashMemItemProvider extends DefaultChooseByNameItemProvider {
    HashMemItemProvider(PsiElement context) {
        super(context)
    }

    @Override
    public boolean filterElements(@NotNull final ChooseByNameBase base,
                                  @NotNull final String pattern,
                                  boolean everywhere,
                                  @NotNull final ProgressIndicator indicator,
                                  @NotNull final Processor<Object> consumer) {
        super.filterElements(base, pattern, everywhere, indicator, consumer)

        consumer.process(SyncHashMemCommand.INSTANCE)
    }
}
