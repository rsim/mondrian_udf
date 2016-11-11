java.util.Date.class_eval do
  def as_json(options = nil)
    Java::ComEazybiMondrianUdf::DateUtils.dateAsJSON(self)
  end
end
