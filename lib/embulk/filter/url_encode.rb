Embulk::JavaPlugin.register_filter(
  "url_encode", "org.embulk.filter.url_encode.UrlEncodeFilterPlugin",
  File.expand_path('../../../../classpath', __FILE__))
