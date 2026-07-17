import { HttpInterceptorFn } from '@angular/common/http';


export const languageInterceptor: HttpInterceptorFn = (req, next) => {

  const lang = localStorage.getItem('lang') ?? 'pt-BR';

  const request = req.clone({
    setHeaders: {
      'Accept-Language': lang
    }
  });

  return next(request);

};
