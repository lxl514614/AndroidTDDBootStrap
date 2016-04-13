/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.piasy.lg.features.splash;

import android.content.Intent;
import android.os.Bundle;
import com.bugtags.library.Bugtags;
import com.bugtags.library.BugtagsOptions;
import com.github.piasy.base.di.HasComponent;
import com.github.piasy.base.utils.RxUtil;
import com.github.piasy.lg.BuildConfig;
import com.github.piasy.lg.LGActivity;
import com.github.piasy.lg.LGApp;
import com.github.piasy.lg.R;
import com.github.piasy.lg.analytics.CrashReportingTree;
import com.github.piasy.lg.features.albums.AlbumsActivity;
import com.github.piasy.lg.features.splash.di.SplashComponent;
import com.github.promeg.androidgitsha.lib.GitShaUtils;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import jonathanfinerty.once.Once;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Piasy{github.com/Piasy} on 15/9/19.
 *
 * Splash activity. Init app and handle other Intent action. I imitate the way in
 * <a href="http://frogermcs.github.io/dagger-graph-creation-performance/">frogermcs'  blog:
 * Dagger
 * 2 - graph creation performance</a> to avoid activity state loss.
 */
@SuppressWarnings({
        "PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity",
        "PMD.ModifiedCyclomaticComplexity"
})
public class SplashActivity extends LGActivity implements HasComponent<SplashComponent> {

    private SplashComponent mSplashComponent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        initialize();
    }

    @Override
    protected void initializeInjector() {
        mSplashComponent = LGApp.get().appComponent().plus();
        mSplashComponent.inject(this);
    }

    private void initialize() {
        Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                final LGApp app = LGApp.get();
                if ("release".equals(BuildConfig.BUILD_TYPE)) {
                    Timber.plant(new CrashReportingTree());
                    BugtagsOptions options = new BugtagsOptions.Builder().trackingLocation(false)
                            .trackingCrashLog(true)
                            .trackingConsoleLog(true)
                            .trackingUserSteps(true)
                            .build();
                    Bugtags.start("82cdb5f7f8925829ccc4a6e7d5d12216", app,
                            Bugtags.BTGInvocationEventShake, options);
                    Bugtags.setUserData("git_sha", GitShaUtils.getGitSha(app));
                } else {
                    Timber.plant(new Timber.DebugTree());
                }

                Iconify.with(new MaterialModule());
                Once.initialise(app);

                return Observable.just(true);
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(final Boolean success) {
                startActivitySafely(new Intent(SplashActivity.this, AlbumsActivity.class));
                finish();
            }
        }, RxUtil.OnErrorLogger);
    }

    @Override
    public SplashComponent getComponent() {
        return mSplashComponent;
    }
}
