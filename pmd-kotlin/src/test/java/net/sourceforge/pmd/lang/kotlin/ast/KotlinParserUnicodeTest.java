/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.kotlin.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.kotlin.ast.KotlinParser.KtFunctionDeclaration;
import net.sourceforge.pmd.lang.kotlin.ast.KotlinParser.KtKotlinFile;
import net.sourceforge.pmd.lang.kotlin.ast.KotlinParser.KtSimpleIdentifier;

/**
 * Tests that Korean (Hangul) identifiers are correctly parsed.
 *
 * <p>The Hangul syllable block (U+AC00..U+D7A3) must be treated as a valid
 * identifier character range. A previous bug in {@code UnicodeClasses.g4}
 * listed only the two endpoints (U+AC00 and U+D7A3) as separate characters
 * instead of a range, so every Hangul syllable between them was rejected by
 * the lexer.
 */
class KotlinParserUnicodeTest {

    @Test
    void testKoreanIdentifiersInFunctionName() {
        // 안 = U+C548, 녕 = U+B155 – both lie in U+AC00..U+D7A3 but are neither
        // the first nor the last character of the range, so they were broken before the fix.
        String code = "fun 안녕(): Int { return 42 }";

        KtKotlinFile root = KotlinParsingHelper.DEFAULT.parse(code);
        assertNotNull(root);

        KtFunctionDeclaration fn = root.descendants(KtFunctionDeclaration.class).first();
        assertNotNull(fn, "Expected a function declaration");

        KtSimpleIdentifier id = fn.descendants(KtSimpleIdentifier.class).first();
        assertNotNull(id, "Expected a simple identifier");
        assertNotNull(id.Identifier(), "Expected Identifier token inside simpleIdentifier");
        assertEquals("안녕", id.Identifier().getText());
    }

    @Test
    void testKoreanIdentifiersInVariableNames() {
        // 변 = U+BCC0, 수 = U+C218 – Hangul syllables in the middle of the range
        String code = "fun foo() { val 변수 = 42 }";

        KtKotlinFile root = KotlinParsingHelper.DEFAULT.parse(code);
        assertNotNull(root);

        KtSimpleIdentifier varId = root.descendants(KtSimpleIdentifier.class)
                .filter(id -> id.Identifier() != null && "변수".equals(id.Identifier().getText()))
                .first();
        assertNotNull(varId, "Expected identifier '변수' to be recognised");
    }
}
