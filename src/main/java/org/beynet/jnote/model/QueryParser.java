package org.beynet.jnote.model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by beynet on 01/05/2015.
 */
public class QueryParser extends org.apache.lucene.queryparser.classic.QueryParser {
    public QueryParser(String f, Analyzer a) {
        super(f, a);
    }

    @Override
    protected Query getWildcardQuery(String field, String termStr) throws ParseException {
        return super.getWildcardQuery(field, termStr);
    }

    @Override
    protected Query getPrefixQuery(String field, String termStr) throws ParseException {
        String newTerm=termStr;
        try {
            StringReader reader = new StringReader(termStr);
            KeywordTokenizer input = new KeywordTokenizer(termStr.length()*2);
            input.setReader(reader);
            TokenStream tokenStream = new ASCIIFoldingFilter(input);
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                newTerm = charTermAttribute.toString();
            }
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.getPrefixQuery(field, newTerm);
    }

    @Override
    protected Query newWildcardQuery(Term t) {
        return super.newWildcardQuery(t);
    }
}
