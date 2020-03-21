package crawler.savers;

import crawler.url.Url;
import crawler.url.Urls;
import com.helger.css.ECSSVersion;
import com.helger.css.writer.CSSWriter;
import com.helger.css.reader.CSSReader;
import com.helger.css.decl.CSSImportRule;
import configFileReader.ConfigFileReader;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.commons.io.resource.URLResource;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;
import com.helger.css.decl.CSSExpressionMemberTermURI;

import java.net.URL;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.BufferedWriter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CssPageSaver extends Saver {

    private String savingPath;
    private Url currentUrl;


    CssPageSaver(Url url, ConfigFileReader config) {
        this.currentUrl = url;
        this.programConfig = config;
        this.savingPath = (this.programConfig.getParameter("savingFolder") + "/" + Urls.removeScheme(currentUrl.toString()));
    }

    @Override
    public ArrayList<Saver> call() throws Exception {

        if (Files.exists(Paths.get(savingPath))) {
            return null;
        }

        //load css file
        CascadingStyleSheet aCSS = CSSReader.readFromStream(
                new URLResource(new URL(currentUrl.toString())),
                new CSSReaderSettings ().setCSSVersion (ECSSVersion.CSS30)
        );

        if (aCSS == null) {
            return null;
        }

        ArrayList<Saver> result = new ArrayList<>();
        result.addAll(createFileSavers(extractFileUrls(aCSS)));
        result.addAll(createCssPageSavers(extractCssLinks(aCSS)));

        //save css file
        new CSSWriter().writeCSS(aCSS, new BufferedWriter(new FileWriter(savingPath)));

        return result;
    }

    private ArrayList<Url> extractCssLinks(CascadingStyleSheet aCSS) {
        ArrayList<Url> result = new ArrayList<>();

        CSSVisitor.visitCSSUrl(aCSS, new DefaultCSSUrlVisitor() {
            @Override
            public void onImport(@Nonnull CSSImportRule aImportRule) {
                result.add(
                        new Url(
                                currentUrl.getRoot() + "/" +
                                        aImportRule.getLocationString().replaceFirst("^/", "")
                        )
                );

                aImportRule.setLocationString(
                                        aImportRule.getLocationString().replaceFirst("^/", "")
                );

            }
        });

        return result;
    }

    private ArrayList<Url> extractFileUrls(CascadingStyleSheet aCSS) {
        ArrayList<Url> result = new ArrayList<>();

        CSSVisitor.visitCSSUrl(aCSS, new DefaultCSSUrlVisitor() {
            @Override
            public void onUrlDeclaration(@Nullable ICSSTopLevelRule aTopLevelRule,
                                         @Nonnull CSSDeclaration aDeclaration,
                                         @Nonnull CSSExpressionMemberTermURI aURITerm) {
                result.add(
                        new Url(
                                currentUrl.removeFileName().toString() +
                                        aURITerm.getURIString().replaceAll("^/", "")
                        )
                );

                aURITerm.setURIString(
                        aURITerm.getURIString().startsWith("/") ?
                                "/" + currentUrl.getHostName() + "/" +
                                        aURITerm.getURIString().replaceAll("^/", "") :
                        currentUrl.removeFileName().toString() +
                                aURITerm.getURIString().replaceAll("^/", "")
                );
            }
        });

        return result;
    }

}