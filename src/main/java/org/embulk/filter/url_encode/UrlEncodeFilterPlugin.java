package org.embulk.filter.url_encode;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.type.Types;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class UrlEncodeFilterPlugin
        implements FilterPlugin
{
    @Override
    public void transaction(ConfigSource config, Schema inputSchema,
            FilterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        control.run(task.dump(), inputSchema);
    }

    @Override
    public PageOutput open(TaskSource taskSource, final Schema inputSchema, final Schema outputSchema, final PageOutput output)
    {
        final PluginTask task = taskSource.loadTask(PluginTask.class);

        return new PageOutput()
        {
            final Logger logger = Exec.getLogger(this.getClass());
            private PageReader reader = new PageReader(inputSchema);
            private PageBuilder builder = new PageBuilder(Exec.getBufferAllocator(), outputSchema, output);

            @Override
            public void add(Page page)
            {
                String column = task.getColumn();

                reader.setPage(page);
                while (reader.nextRecord()) {
                    List<Column> columns = inputSchema.getColumns();
                    for (int i = 0; i < columns.size(); i++) {
                        Column inputColumn = columns.get(i);

                        if (reader.isNull(inputColumn)) {
                            builder.setNull(i);
                        }
                        else {
                            if (Types.STRING.equals(inputColumn.getType())) {
                                String url = reader.getString(inputColumn);

                                if (column.equals(inputColumn.getName())) {
                                    if (task.getOnlyNonAscii()) {
                                        StringBuilder sb = new StringBuilder();

                                        for (char ch : url.toCharArray()) {
                                            if (isAsciiPrintable(ch)) {
                                                sb.append(ch);
                                            }
                                            else {
                                                if (ch == ' ') {
                                                    sb.append("%20");
                                                }
                                                else {
                                                    appendUrlEncoded(sb, ch);
                                                }
                                            }
                                        }

                                        url = sb.toString();
                                    }
                                    else {
                                        try {
                                            url = URLEncoder.encode(url, "UTF-8").replace("+", "%20");
                                        }
                                        catch (UnsupportedEncodingException e) {
                                            // do nothing
                                        }
                                    }
                                }

                                builder.setString(i, url);
                            }
                            else if (Types.BOOLEAN.equals(inputColumn.getType())) {
                                builder.setBoolean(i, reader.getBoolean(inputColumn));
                            }
                            else if (Types.DOUBLE.equals(inputColumn.getType())) {
                                builder.setDouble(i, reader.getDouble(inputColumn));
                            }
                            else if (Types.LONG.equals(inputColumn.getType())) {
                                builder.setLong(i, reader.getLong(inputColumn));
                            }
                            else if (Types.TIMESTAMP.equals(inputColumn.getType())) {
                                builder.setTimestamp(i, reader.getTimestamp(inputColumn));
                            }
                        }
                    }
                    builder.addRecord();
                }
            }

            private void appendUrlEncoded(StringBuilder buff, char ch)
            {
                byte[] bytes = String.valueOf(ch).getBytes();
                for (byte b : bytes) {
                    buff.append('%')
                            .append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)))
                            .append(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
                }
            }

            private boolean isAsciiPrintable(char ch)
            {
                return ch > 32 && ch < 127;
            }

            @Override
            public void finish()
            {
                builder.finish();
            }

            @Override
            public void close()
            {
                builder.close();
            }
        };
    }

    public interface PluginTask
            extends Task
    {
        @Config("column")
        public String getColumn();

        @Config("only_non_ascii")
        @ConfigDefault("false")
        public boolean getOnlyNonAscii();
    }
}
